# AzerothCore website backend

The API behind an AzerothCore server's website: account sign-up (with captcha), password reset by e-mail, a store
(work in progress), and optional [Minecraft accounts](#minecraft-accounts-optional). Pull requests welcome.

## Setup

```bash
git clone https://github.com/EngineeringOV/Website-API.git
```
```bash
cd Website-API && ./setup.sh
```

It asks whether AzerothCore runs in Docker or directly on the machine, then for the database password, the website
address, your reCAPTCHA secret and the mail account used for password resets. It writes the configuration and, on
bare metal, also creates the database user and installs Java and the GMP library. It can also get an HTTPS
certificate. Run it again to change anything.

## Start

Docker, next to AzerothCore's containers:

```bash
docker compose up -d
```

With the website itself (nginx and HTTPS) as well:

```bash
docker compose --profile nginx up -d
```

Bare metal:

```bash
./gradlew bootWar && java -jar build/libs/API.war
```

Renew the certificate later with `sudo certbot renew --webroot -w /var/www/certbot`.

## Minecraft accounts (optional)

Lets players log in to Minecraft with their WoW account, through [mc-launch](https://github.com/EngineeringOV/mc-launch).
Real Minecraft accounts keep working on the same servers. Off by default.

`./setup.sh` asks about it. To change it later, edit `.env` (Docker) and restart the API:

```
MINECRAFT_AUTH_ENABLED=true
MINECRAFT_AUTH_NAME=My Server
MINECRAFT_GAME_SERVER=play.example.com
MINECRAFT_AUTH_NAMECLASH=suffix
```

The name is shown in the launcher. The game server defaults to this website's host. `suffix` is optional and
needs mc-launch's server plugin (see below).

Without Docker: `minecraft.auth.enabled=true` (and `minecraft.auth.name`, `minecraft.auth.game-server`,
`minecraft.auth.name-clash`) in `application-prod.properties`. The `acore_custom.mc_*` tables are created on start.
Check it with `curl https://example.com/api/minecraft`.

- **Players**: in mc-launch, *Add account* with `example.com`, their WoW name and password. Banned or locked
  accounts can't log in. If the WoW name can't be a Minecraft name (3-16 letters, digits, `_`), they pick one once.
- **Game server**: set up with RealmMover's *Set up a new Minecraft server* and enter `example.com` as the auth
  server; it adds the needed flags.
- **Same name as a real account**: yours wins and the real account is refused. With `name-clash=suffix` and
  mc-launch's server plugin on the game server, the real one joins as `Bob(2)` and yours shows as `Bob(1)`.

Everything lives under `/api/minecraft` in Mojang's own formats; tokens are stored hashed and failed logins are
rate limited.
