#!/usr/bin/env bash
# One-time setup of the website API: asks a few questions and writes the configuration.
#   ./setup.sh            asks whether you use Docker or not
#   ./setup.sh --docker   Docker (API next to AzerothCore's containers)
#   ./setup.sh --bare     bare metal (API, MySQL and Java on this machine)
# Answers can be given beforehand as environment variables (WAPI_SPRING_DB_PW=... ./setup.sh) to skip the questions.
set -euo pipefail
cd "$(dirname "$0")"

say() { printf '\n\033[1m%s\033[0m\n' "$*"; }
ok() { printf '  \033[32m✔\033[0m %s\n' "$*"; }
die() { printf '\033[31m%s\033[0m\n' "$*" >&2; exit 1; }

# ask VAR "Question" [default] [secret]: keeps a value already set in the environment (even an empty one)
ask() {
  local var=$1 question=$2 default=${3-} secret=${4-} answer
  if [ -n "${!var+x}" ]; then return; fi
  answer=""
  if [ -n "$secret" ]; then
    read -rsp "  $question${default:+ [$default]}: " answer || true; echo
  else
    read -rp "  $question${default:+ [$default]}: " answer || true
  fi
  printf -v "$var" '%s' "${answer:-$default}"
}

yes_no() { # yes_no VAR "Question" default(y/n)
  ask "$1" "$2 (y/n)" "$3"
  case "${!1}" in [yY]*) printf -v "$1" y ;; *) printf -v "$1" n ;; esac
}

SUDO=""
[ "$(id -u)" -eq 0 ] || SUDO="sudo"

# --- mode -----------------------------------------------------------------------------------
MODE="${1-}"
case "$MODE" in
  --docker) WAPI_MODE=docker ;;
  --bare) WAPI_MODE=bare ;;
  "") ;;
  *) die "Unknown option $MODE (use --docker or --bare)" ;;
esac
if [ -z "${WAPI_MODE-}" ]; then
  say "How do you run AzerothCore?"
  echo "  1) Docker (azerothcore-wotlk's docker compose)"
  echo "  2) Directly on this machine (bare metal)"
  ask WAPI_MODE_CHOICE "Choose" 1
  [ "$WAPI_MODE_CHOICE" = 2 ] && WAPI_MODE=bare || WAPI_MODE=docker
fi

if ! command -v envsubst >/dev/null; then
  say "Installing gettext (for envsubst)"
  $SUDO apt-get install -y gettext-base >/dev/null || die "Install gettext yourself (sudo apt install gettext) and run again."
fi

for f in src/main/resources/application-prod.properties .env; do
  if [ -e "$f" ] && [ -z "${WAPI_OVERWRITE-}" ]; then
    yes_no WAPI_OVERWRITE "$f already exists. Overwrite the configuration?" n
    [ "$WAPI_OVERWRITE" = y ] || die "Nothing changed."
  fi
done

# --- questions ------------------------------------------------------------------------------
say "Database"
[ "$WAPI_MODE" = docker ] && ask WAPI_DB_ROOT_PW "AzerothCore database root password" password secret
ask WAPI_SPRING_DB_PW "Password for the API's own database user 'spring'" password secret

say "Website"
ask WAPI_WEBSITE_URL "Website URL" https://example.com
WAPI_WEBSITE_URL="${WAPI_WEBSITE_URL%/}"
WAPI_DOMAIN=$(echo "$WAPI_WEBSITE_URL" | sed -E 's|^https?://||; s|/.*$||')
ask WAPI_SUBDOMAIN "Subdomain for the certificate (empty for none)" ""
WAPI_CERT_DOMAIN="${WAPI_SUBDOMAIN:+$WAPI_SUBDOMAIN.}$WAPI_DOMAIN"
ask WAPI_CAPTCHA_PRIVATE "reCAPTCHA secret key" "" secret
[ -n "$WAPI_CAPTCHA_PRIVATE" ] || echo "  (no captcha key: account creation will fail until you add one)"

say "Mail (password resets)"
ask WAPI_MAIL_USER "Mail username" hello@example.org
ask WAPI_MAIL_HOST "Mail server" mail.gandi.net
ask WAPI_MAIL_PW "Mail password" password secret

say "Minecraft accounts (players log in to Minecraft with their WoW account)"
yes_no WAPI_MC "Turn them on?" n
if [ "$WAPI_MC" = y ]; then
  ask WAPI_MC_NAME "Name shown in the launcher" "My Server"
  ask WAPI_MC_GAME_SERVER "Minecraft server address (empty = $WAPI_DOMAIN)" ""
  yes_no WAPI_MC_SUFFIX "Let a real Minecraft account with one of your names in as Name(2)? Needs mc-launch's server plugin" n
fi
MC_ENABLED=false; MC_CLASH=refuse
if [ "$WAPI_MC" = y ]; then MC_ENABLED=true; [ "$WAPI_MC_SUFFIX" = y ] && MC_CLASH=suffix; fi

# --- configuration files --------------------------------------------------------------------
say "Writing the configuration"
if [ "$WAPI_MODE" = docker ]; then DB_URL="jdbc:mysql://ac-database:3306/acore_auth"; else DB_URL="jdbc:mysql://localhost:3306/acore_auth"; fi

prod=src/main/resources/application-prod.properties
SPRING_DATASOURCE_PASSWORD="$WAPI_SPRING_DB_PW" SPRING_DATASOURCE_URL="$DB_URL" GOOGLE_CAPTCHA_PRIVATE="$WAPI_CAPTCHA_PRIVATE" \
SPRING_MAIL_USERNAME="$WAPI_MAIL_USER" SPRING_MAIL_PASSWORD="$WAPI_MAIL_PW" SPRING_MAIL_HOST="$WAPI_MAIL_HOST" API_WEBSITE_URL="$WAPI_WEBSITE_URL" \
  envsubst '${SPRING_DATASOURCE_PASSWORD} ${SPRING_DATASOURCE_URL} ${GOOGLE_CAPTCHA_PRIVATE} ${SPRING_MAIL_USERNAME} ${SPRING_MAIL_PASSWORD} ${SPRING_MAIL_HOST} ${API_WEBSITE_URL}' \
  < src/main/resources/application-prod.properties.template > "$prod"
# the Minecraft lines keep ${VAR:default} so Docker's environment can still override them
esc() { printf '%s' "$1" | sed 's/[\/&|]/\\&/g'; }
sed -i \
  -e "s|\${MINECRAFT_AUTH_ENABLED:false}|\${MINECRAFT_AUTH_ENABLED:$MC_ENABLED}|" \
  -e "s|\${MINECRAFT_AUTH_NAME:Minecraft}|\${MINECRAFT_AUTH_NAME:$(esc "${WAPI_MC_NAME:-Minecraft}")}|" \
  -e "s|\${MINECRAFT_GAME_SERVER:}|\${MINECRAFT_GAME_SERVER:$(esc "${WAPI_MC_GAME_SERVER-}")}|" \
  -e "s|\${MINECRAFT_AUTH_NAMECLASH:refuse}|\${MINECRAFT_AUTH_NAMECLASH:$MC_CLASH}|" "$prod"
chmod 600 "$prod"
ok "$prod"

if [ "$WAPI_MODE" = docker ]; then
  DOCKER_DB_ROOT_PASSWORD="$WAPI_DB_ROOT_PW" DOCKER_SPRING_DB_PASSWORD="$WAPI_SPRING_DB_PW" DOMAIN="$WAPI_DOMAIN" \
  SUBDOMAIN="$WAPI_SUBDOMAIN" CERT_DOMAIN="$WAPI_CERT_DOMAIN" \
    envsubst '${DOCKER_DB_ROOT_PASSWORD} ${DOCKER_SPRING_DB_PASSWORD} ${DOMAIN} ${SUBDOMAIN} ${CERT_DOMAIN}' < .env.template > .env
  {
    echo
    echo "# Minecraft accounts"
    echo "MINECRAFT_AUTH_ENABLED=$MC_ENABLED"
    echo "MINECRAFT_AUTH_NAME=${WAPI_MC_NAME:-Minecraft}"
    echo "MINECRAFT_GAME_SERVER=${WAPI_MC_GAME_SERVER-}"
    echo "MINECRAFT_AUTH_NAMECLASH=$MC_CLASH"
  } >> .env
  chmod 600 .env
  ok ".env"
fi

# --- bare metal: Java, database, GMP -----------------------------------------------------------
if [ "$WAPI_MODE" = bare ]; then
  if ! command -v javac >/dev/null || ! java -version 2>&1 | grep -qE 'version "(1[7-9]|[2-9][0-9])'; then
    say "Java"
    $SUDO apt-get install -y -qq openjdk-21-jdk-headless >/dev/null && ok "Java 21" || die "Install a JDK 17 or newer and run again."
  fi

  say "Database user and tables"
  # -f like the Docker entrypoint: on a re-run, keys and constraints that already exist are fine
  SPRING_HOST=localhost SPRING_PASSWORD="$WAPI_SPRING_DB_PW" envsubst '${SPRING_HOST} ${SPRING_PASSWORD}' < sql/init.sql.template \
    | $SUDO mysql -f >/dev/null 2>&1 || true
  MYSQL_PWD="$WAPI_SPRING_DB_PW" mysql -u spring -h localhost -e "SELECT 1 FROM acore_custom.store_package_base LIMIT 0" >/dev/null 2>&1 \
    || die "Could not set up the database: is MySQL/MariaDB running, and does 'sudo mysql' work?"
  ok "user 'spring' and the acore_custom tables"

  say "GMP (used to check WoW passwords)"
  $SUDO apt-get install -y -qq gcc make libgmp-dev >/dev/null
  [ -d lib/GMP-java ] || git clone -q https://github.com/EngineeringOV/GMP-java.git lib/GMP-java
  JAVA_HOME="${JAVA_HOME:-$(dirname "$(dirname "$(readlink -f "$(command -v javac || command -v java)")")")}" \
    make -s -C lib/GMP-java >lib/gmp-build.log 2>&1 || { cat lib/gmp-build.log; die "Building GMP failed (needs a JDK: sudo apt install openjdk-21-jdk-headless)."; }
  $SUDO install -o root -g root -m 755 lib/GMP-java/libjcl.so lib/GMP-java/libnativegmp.so /lib/
  ok "libjcl.so and libnativegmp.so in /lib"

fi

# --- HTTPS ----------------------------------------------------------------------------------
say "HTTPS certificate for $WAPI_CERT_DOMAIN"
yes_no WAPI_CERT "Get one now with certbot? (port 80 must be free and the domain must point here)" n
if [ "$WAPI_CERT" = y ]; then
  command -v certbot >/dev/null || $SUDO snap install --classic certbot
  $SUDO certbot certonly --standalone -d "$WAPI_CERT_DOMAIN" && ok "certificate" || echo "  certbot failed; run it again later."
fi

# --- done -----------------------------------------------------------------------------------
say "Done. Start it with:"
if [ "$WAPI_MODE" = docker ]; then
  echo "  docker compose up -d                    # the API only"
  echo "  docker compose --profile nginx up -d    # the API and the website (nginx, HTTPS)"
else
  echo "  ./gradlew bootWar && java -jar build/libs/API.war"
fi
[ "$WAPI_MC" = y ] && echo "Then check Minecraft accounts: curl $WAPI_WEBSITE_URL/api/minecraft"
echo "Certificate renewal later: ${SUDO:+$SUDO }certbot renew --webroot -w /var/www/certbot"
