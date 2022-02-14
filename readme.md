# AzerothCore website backend

## NOTES
- This project was never fully shipped.
- Account creation should work.
- You DO want some kind of captcha for the store
- I make no promises that the DB looks like it is supposed too, so you will want to verify that and if you use this on a production server use a Hibernate config that verifies ONLY
- Do a text in all files search for "FIXME" to find some things that could be improved
- The store part and it's parts weren't tested properly, this includes creating item instance and mail etc etc.
- Feel free to make PRs

## RUNNING LOCALLY (One time setup)


INSTALL SQL & GMP 

1: Logging into mysql
```bash
sudo mysql -u root
```

2: Creating Mysql user (Replace with your password), creates tables and setting privileges for our new user
###  Replace with your password in the first line (match it to the environment variable with the key "spring.datasource.password" in your .properties file)
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

GRANT ALL  PRIVILEGES ON acore_custom.* TO 'spring'@'localhost';
GRANT ALL  PRIVILEGES ON acore_auth.* TO 'spring'@'localhost';
GRANT ALL  PRIVILEGES ON acore_characters.* TO 'spring'@'localhost';
GRANT ALL  PRIVILEGES ON acore_world.* TO 'spring'@'localhost';

exit
```

3: Installing GMP (From project root and assuming Debian/Ubuntu) 
```bash
mkdir lib
cd lib
git clone https://github.com/EngineeringOV/GMP-java.git
cd GMP-java
make
 
sudo cp libjcl.so /lib
sudo cp libnativegmp.so /lib
sudo chown root:root  /lib/libjcl.so
sudo chown root:root  /lib/libnativegmp.so
sudo chmod 755  /lib/libjcl.so
sudo chmod 755  /lib/libnativegmp.so
```

