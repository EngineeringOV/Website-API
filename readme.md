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

### 1: Logging into MySQL (via AzerothCore Docker container)

```bash
docker exec -it ac-database mysql --user=root --password=<your-root-password>
```

### 2: Creating MySQL user & tables

Replace the password in the first line (match it to `spring.datasource.password` in your `.properties` file):

```mysql
CREATE USER 'spring'@'%' IDENTIFIED BY '!!REPLACE-ME!!';

CREATE SCHEMA IF NOT EXISTS acore_world;
CREATE SCHEMA IF NOT EXISTS acore_characters;
CREATE SCHEMA IF NOT EXISTS acore_auth;
CREATE SCHEMA IF NOT EXISTS acore_custom;

CREATE TABLE IF NOT EXISTS acore_custom.account_reset_request (`uuid` VARCHAR(255) NOT NULL, created_at datetime, email VARCHAR(255), ip_address VARCHAR(255), valid_request TINYINT, primary key (`uuid`)) engine=InnoDB;
CREATE TABLE IF NOT EXISTS acore_custom.store_account_tokens (`uuid` VARCHAR(255) NOT NULL, free_token integer, premium_token integer, vote_token integer, account_id INT, primary key (`uuid`)) engine=InnoDB;
CREATE TABLE IF NOT EXISTS acore_custom.store_package_availability (`uuid` VARCHAR(255) NOT NULL, current_price bigint, `current_price_units` VARCHAR(255), ends_at datetime, starts_at datetime, item_base VARCHAR(255), primary key (`uuid`)) engine=InnoDB;
CREATE TABLE IF NOT EXISTS acore_custom.store_package_base (`uuid` VARCHAR(255) NOT NULL, copper integer, `full_price` bigint, `image_url` VARCHAR(255), `name_package` VARCHAR(255), `price_units` VARCHAR(255), `subtext` VARCHAR(255), `type` VARCHAR(255), primary key (`uuid`)) engine=InnoDB;
CREATE TABLE IF NOT EXISTS acore_custom.store_package_item (`uuid` VARCHAR(255) NOT NULL, item_id_alliance integer, item_id_horde integer, quantity_alliance integer, quantity_horde integer, `item_base_uuid` VARCHAR(255), primary key (`uuid`)) engine=InnoDB;
ALTER TABLE acore_custom.store_account_tokens ADD CONSTRAINT FKrrdc41fys57mnbc61c9v2jpan FOREIGN KEY (account_id) REFERENCES acore_auth.account (id);
ALTER TABLE acore_custom.store_package_availability ADD CONSTRAINT FKt0ob82dwamt56ee4ilk0fudo8 FOREIGN KEY (item_base) REFERENCES acore_custom.store_package_base (`uuid`);
ALTER TABLE acore_custom.store_package_item ADD CONSTRAINT FKliyh2h1dvh86rsu89dupb7xfy FOREIGN KEY (`item_base_uuid`) REFERENCES acore_custom.store_package_base (`uuid`);

GRANT ALL PRIVILEGES ON acore_custom.* TO 'spring'@'%';
GRANT ALL PRIVILEGES ON acore_auth.* TO 'spring'@'%';
GRANT ALL PRIVILEGES ON acore_characters.* TO 'spring'@'%';
GRANT ALL PRIVILEGES ON acore_world.* TO 'spring'@'%';

exit
```

### 3: HTTPS / SSL Setup (nginx only)

Install Certbot and issue a certificate (before starting nginx for the first time):
```bash
sudo apt install certbot
sudo certbot certonly --standalone -d your-domain.com
```

Set your domain in `.env`:
```
DOMAIN=your-domain.com
```

Renewal (if container is already running):
```bash
sudo certbot renew --webroot -w /var/www/certbot
```

### 4: Start

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

### 0: Install Java 18 (optional)

```bash
wget https://download.java.net/java/GA/jdk18.0.2.1/db379da656dc47308e138f21b33976fa/1/GPL/openjdk-18.0.2.1_linux-x64_bin.tar.gz
tar xzf openjdk-18.0.2.1_linux-x64_bin.tar.gz
sudo mv jdk-18.0.2.1 /usr/lib/jvm/
sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/jdk-18.0.2.1/bin/java 2
sudo update-alternatives --install /usr/bin/javac javac /usr/lib/jvm/jdk-18.0.2.1/bin/javac 2
export JAVA_HOME=/usr/lib/jvm/jdk-18.0.2.1
```

### 1: Install MySQL & GMP

```bash
sudo apt install mysql-server
sudo apt install gcc libgmp-dev
```

### 2: Logging into MySQL

```bash
sudo mysql --user=root --password
```

### 3: Creating MySQL user & tables

Replace the password in the first line (match it to `spring.datasource.password` in your `.properties` file):

```mysql
CREATE USER 'spring'@'localhost' IDENTIFIED BY '!!REPLACE-ME!!';

CREATE SCHEMA IF NOT EXISTS acore_world;
CREATE SCHEMA IF NOT EXISTS acore_characters;
CREATE SCHEMA IF NOT EXISTS acore_auth;
CREATE SCHEMA IF NOT EXISTS acore_custom;

CREATE TABLE IF NOT EXISTS acore_custom.account_reset_request (`uuid` VARCHAR(255) NOT NULL, created_at datetime, email VARCHAR(255), ip_address VARCHAR(255), valid_request TINYINT, primary key (`uuid`)) engine=InnoDB;
CREATE TABLE IF NOT EXISTS acore_custom.store_account_tokens (`uuid` VARCHAR(255) NOT NULL, free_token integer, premium_token integer, vote_token integer, account_id INT, primary key (`uuid`)) engine=InnoDB;
CREATE TABLE IF NOT EXISTS acore_custom.store_package_availability (`uuid` VARCHAR(255) NOT NULL, current_price bigint, `current_price_units` VARCHAR(255), ends_at datetime, starts_at datetime, item_base VARCHAR(255), primary key (`uuid`)) engine=InnoDB;
CREATE TABLE IF NOT EXISTS acore_custom.store_package_base (`uuid` VARCHAR(255) NOT NULL, copper integer, `full_price` bigint, `image_url` VARCHAR(255), `name_package` VARCHAR(255), `price_units` VARCHAR(255), `subtext` VARCHAR(255), `type` VARCHAR(255), primary key (`uuid`)) engine=InnoDB;
CREATE TABLE IF NOT EXISTS acore_custom.store_package_item (`uuid` VARCHAR(255) NOT NULL, item_id_alliance integer, item_id_horde integer, quantity_alliance integer, quantity_horde integer, `item_base_uuid` VARCHAR(255), primary key (`uuid`)) engine=InnoDB;
ALTER TABLE acore_custom.store_account_tokens ADD CONSTRAINT FKrrdc41fys57mnbc61c9v2jpan FOREIGN KEY (account_id) REFERENCES acore_auth.account (id);
ALTER TABLE acore_custom.store_package_availability ADD CONSTRAINT FKt0ob82dwamt56ee4ilk0fudo8 FOREIGN KEY (item_base) REFERENCES acore_custom.store_package_base (`uuid`);
ALTER TABLE acore_custom.store_package_item ADD CONSTRAINT FKliyh2h1dvh86rsu89dupb7xfy FOREIGN KEY (`item_base_uuid`) REFERENCES acore_custom.store_package_base (`uuid`);

GRANT ALL PRIVILEGES ON acore_custom.* TO 'spring'@'localhost';
GRANT ALL PRIVILEGES ON acore_auth.* TO 'spring'@'localhost';
GRANT ALL PRIVILEGES ON acore_characters.* TO 'spring'@'localhost';
GRANT ALL PRIVILEGES ON acore_world.* TO 'spring'@'localhost';

exit
```

### 4: Install GMP (from project root, assuming Debian/Ubuntu)

```bash
mkdir lib
cd lib
git clone https://github.com/EngineeringOV/GMP-java.git
cd GMP-java
make

sudo cp libjcl.so /lib
sudo cp libnativegmp.so /lib
sudo chown root:root /lib/libjcl.so
sudo chown root:root /lib/libnativegmp.so
sudo chmod 755 /lib/libjcl.so
sudo chmod 755 /lib/libnativegmp.so
cd ../..
```

### 5: HTTPS / SSL Setup (nginx only)

Install Certbot and issue a certificate (before starting nginx for the first time):
```bash
sudo apt install certbot
sudo certbot certonly --standalone -d your-domain.com
```

Set your domain in `.env`:
```
DOMAIN=your-domain.com
```

Renewal (if container is already running):
```bash
sudo certbot renew --webroot -w /var/www/certbot
```

### 6: Start

```bash
./gradlew bootWar
java -jar build/libs/API.war
```

</details>

---
