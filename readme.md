# AzerothCore website backend

## NOTES
- Use Java 18
- You DO want some kind of captcha
- The store part is WIP NYI.
- Feel free to make PRs

---

## SET UP (One time setup)

Requires `gettext` (`sudo apt install gettext` on Debian/Ubuntu).

<details>
<summary><strong>🐳 Docker Setup</strong></summary>

### 1: Configuration

```bash
printf "DB root password [password]: " && read -r WAPI_DB_ROOT_PW && echo
WAPI_DB_ROOT_PW="${WAPI_DB_ROOT_PW:-password}"
printf "Spring DB password [password]: " && read -r WAPI_SPRING_DB_PW && echo
WAPI_SPRING_DB_PW="${WAPI_SPRING_DB_PW:-password}"
printf "reCAPTCHA secret key: " && read -r WAPI_CAPTCHA_PRIVATE && echo
printf "Mail username [hello@example.org]: " && read -r WAPI_MAIL_USER
WAPI_MAIL_USER="${WAPI_MAIL_USER:-hello@example.org}"
printf "Mail host [mail.gandi.net]: " && read -r WAPI_MAIL_HOST
WAPI_MAIL_HOST="${WAPI_MAIL_HOST:-mail.gandi.net}"
printf "Mail server password [password]: " && read -r WAPI_MAIL_PW && echo
WAPI_MAIL_PW="${WAPI_MAIL_PW:-password}"
printf "Website URL [https://example.com]: " && read -r WAPI_WEBSITE_URL
WAPI_WEBSITE_URL="${WAPI_WEBSITE_URL:-https://example.com}"
WAPI_DOMAIN=$(echo "$WAPI_WEBSITE_URL" | sed 's|https\?://||')
printf "Subdomain (leave empty for none): " && read -r WAPI_SUBDOMAIN

if [ -n "$WAPI_SUBDOMAIN" ]; then
  WAPI_CERT_DOMAIN="${WAPI_SUBDOMAIN}.${WAPI_DOMAIN}"
else
  WAPI_CERT_DOMAIN="$WAPI_DOMAIN"
fi

DOCKER_DB_ROOT_PASSWORD="$WAPI_DB_ROOT_PW" \
DOCKER_SPRING_DB_PASSWORD="$WAPI_SPRING_DB_PW" \
DOMAIN="$WAPI_DOMAIN" \
SUBDOMAIN="$WAPI_SUBDOMAIN" \
CERT_DOMAIN="$WAPI_CERT_DOMAIN" \
  envsubst '${DOCKER_DB_ROOT_PASSWORD} ${DOCKER_SPRING_DB_PASSWORD} ${DOMAIN} ${SUBDOMAIN} ${CERT_DOMAIN}' < .env.template > .env

SPRING_DATASOURCE_PASSWORD="$WAPI_SPRING_DB_PW" \
SPRING_DATASOURCE_URL="jdbc:mysql://ac-database:3306/acore_auth" \
GOOGLE_CAPTCHA_PRIVATE="$WAPI_CAPTCHA_PRIVATE" \
SPRING_MAIL_USERNAME="$WAPI_MAIL_USER" \
SPRING_MAIL_PASSWORD="$WAPI_MAIL_PW" \
SPRING_MAIL_HOST="$WAPI_MAIL_HOST" \
API_WEBSITE_URL="$WAPI_WEBSITE_URL" \
  envsubst '${SPRING_DATASOURCE_PASSWORD} ${SPRING_DATASOURCE_URL} ${GOOGLE_CAPTCHA_PRIVATE} ${SPRING_MAIL_USERNAME} ${SPRING_MAIL_PASSWORD} ${SPRING_MAIL_HOST} ${API_WEBSITE_URL}' \
  < src/main/resources/application-prod.properties.template \
  > src/main/resources/application-prod.properties
```

### 2: HTTPS / SSL Setup (nginx only)

Install Certbot and issue a certificate (before starting nginx for the first time):
```bash
sudo snap install --classic certbot
sudo certbot certonly --standalone -d "$WAPI_CERT_DOMAIN"
```

Renewal (if container is already running):
```bash
sudo certbot renew --webroot -w /var/www/certbot
```

### 3: Start

```bash
docker compose up
```

#### Start API + nginx webserver (if you have a website)
```bash
docker compose --profile nginx up
```

</details>

---

<details>
<summary><strong>🖥️ Bare Metal Setup</strong></summary>

### 0: Install Java 18

<details>
<summary>If Java 18+ is not already installed</summary>

```bash
wget https://download.java.net/java/GA/jdk18.0.2.1/db379da656dc47308e138f21b33976fa/1/GPL/openjdk-18.0.2.1_linux-x64_bin.tar.gz
tar xzf openjdk-18.0.2.1_linux-x64_bin.tar.gz
sudo mv jdk-18.0.2.1 /usr/lib/jvm/
sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/jdk-18.0.2.1/bin/java 2
sudo update-alternatives --install /usr/bin/javac javac /usr/lib/jvm/jdk-18.0.2.1/bin/javac 2
export JAVA_HOME=/usr/lib/jvm/jdk-18.0.2.1
```

</details>

### 1: Configuration

```bash
printf "Spring DB password [password]: " && read -r WAPI_SPRING_DB_PW && echo
WAPI_SPRING_DB_PW="${WAPI_SPRING_DB_PW:-password}"
printf "reCAPTCHA secret key: " && read -r WAPI_CAPTCHA_PRIVATE && echo
printf "Mail username [hello@example.org]: " && read -r WAPI_MAIL_USER
WAPI_MAIL_USER="${WAPI_MAIL_USER:-hello@example.org}"
printf "Mail host [mail.gandi.net]: " && read -r WAPI_MAIL_HOST
WAPI_MAIL_HOST="${WAPI_MAIL_HOST:-mail.gandi.net}"
printf "Mail server password [password]: " && read -r WAPI_MAIL_PW && echo
WAPI_MAIL_PW="${WAPI_MAIL_PW:-password}"
printf "Website URL [https://example.com]: " && read -r WAPI_WEBSITE_URL
WAPI_WEBSITE_URL="${WAPI_WEBSITE_URL:-https://example.com}"
WAPI_DOMAIN=$(echo "$WAPI_WEBSITE_URL" | sed 's|https\?://||')
printf "Subdomain (leave empty for none): " && read -r WAPI_SUBDOMAIN

if [ -n "$WAPI_SUBDOMAIN" ]; then
  WAPI_CERT_DOMAIN="${WAPI_SUBDOMAIN}.${WAPI_DOMAIN}"
else
  WAPI_CERT_DOMAIN="$WAPI_DOMAIN"
fi

SPRING_DATASOURCE_PASSWORD="$WAPI_SPRING_DB_PW" \
SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/acore_auth" \
GOOGLE_CAPTCHA_PRIVATE="$WAPI_CAPTCHA_PRIVATE" \
SPRING_MAIL_USERNAME="$WAPI_MAIL_USER" \
SPRING_MAIL_PASSWORD="$WAPI_MAIL_PW" \
SPRING_MAIL_HOST="$WAPI_MAIL_HOST" \
API_WEBSITE_URL="$WAPI_WEBSITE_URL" \
  envsubst '${SPRING_DATASOURCE_PASSWORD} ${SPRING_DATASOURCE_URL} ${GOOGLE_CAPTCHA_PRIVATE} ${SPRING_MAIL_USERNAME} ${SPRING_MAIL_PASSWORD} ${SPRING_MAIL_HOST} ${API_WEBSITE_URL}' \
  < src/main/resources/application-prod.properties.template \
  > src/main/resources/application-prod.properties
```

### 2: Create MySQL user & tables

```bash
SPRING_HOST=localhost SPRING_PASSWORD="$WAPI_SPRING_DB_PW" \
  envsubst '${SPRING_HOST} ${SPRING_PASSWORD}' < sql/init.sql.template | sudo mysql
```

### 3: Install GMP (from project root, assuming Debian/Ubuntu)

```bash
sudo apt install gcc libgmp-dev

git clone https://github.com/EngineeringOV/GMP-java.git lib/GMP-java
cd lib/GMP-java
make

sudo cp libjcl.so /lib
sudo cp libnativegmp.so /lib
sudo chown root:root /lib/libjcl.so
sudo chown root:root /lib/libnativegmp.so
sudo chmod 755 /lib/libjcl.so
sudo chmod 755 /lib/libnativegmp.so
cd ../..
```

### 4: HTTPS / SSL Setup (nginx only)

Install Certbot and issue a certificate (before starting nginx for the first time):
```bash
sudo snap install --classic certbot
sudo certbot certonly --standalone -d "$WAPI_CERT_DOMAIN"
```

Renewal (if container is already running):
```bash
sudo certbot renew --webroot -w /var/www/certbot
```

### 5: Start

```bash
./gradlew bootWar
java -jar build/libs/API.war
```

</details>

---
