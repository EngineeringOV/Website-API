# AzerothCore website backend

## NOTES
- Use Java 18
- You DO want some kind of captcha
- The store part is WIP NYI.
- Feel free to make PRs

---

## SET UP (One time setup)

<details>
<summary><strong>🐳 Docker Setup</strong></summary>

### 1: Configure passwords

Set your database passwords in the `.env` file:

```
DOCKER_DB_ROOT_PASSWORD=your_root_password
DOCKER_SPRING_DB_PASSWORD=your_spring_password
```

The `entrypoint.sh` script automatically creates the `spring` MySQL user, schemas, and tables when the container starts using `sql/init.sql.template`. No manual SQL is needed.

### 2: HTTPS / SSL Setup (nginx only)

Install Certbot and issue a certificate (before starting nginx for the first time):
```bash
read -rp "Domain (e.g. example.com): " WAPI_DOMAIN
sudo apt install certbot
sudo certbot certonly --standalone -d "$WAPI_DOMAIN"
sed -i "s/DOMAIN=.*/DOMAIN=$WAPI_DOMAIN/" .env
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

### 1: Create MySQL user & tables

Choose a new password for the `spring` MySQL user. This is **not** an existing AzerothCore password — you are creating it now. It must match `spring.datasource.password` in your `.properties` file.

Requires `gettext` for `envsubst` (`sudo apt install gettext` on Debian/Ubuntu).

```bash
read -rsp "New Spring DB password: " WAPI_SPRING_PW && echo
SPRING_HOST=localhost SPRING_PASSWORD="$WAPI_SPRING_PW" \
  envsubst '${SPRING_HOST} ${SPRING_PASSWORD}' < sql/init.sql.template | sudo mysql
```

### 2: Install GMP (from project root, assuming Debian/Ubuntu)

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

### 3: HTTPS / SSL Setup (nginx only)

Install Certbot and issue a certificate (before starting nginx for the first time):
```bash
read -rp "Domain (e.g. example.com): " WAPI_DOMAIN
sudo apt install certbot
sudo certbot certonly --standalone -d "$WAPI_DOMAIN"
sed -i "s/DOMAIN=.*/DOMAIN=$WAPI_DOMAIN/" .env
```

Renewal (if container is already running):
```bash
sudo certbot renew --webroot -w /var/www/certbot
```

### 4: Start

```bash
./gradlew bootWar
java -jar build/libs/API.war
```

</details>

---
