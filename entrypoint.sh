#!/bin/bash
set -e

echo "Waiting for database..."
until mysql -h ac-database -u root -p"$DB_ROOT_PASSWORD" -e "SELECT 1" &>/dev/null; do
  sleep 2
done

echo "Initializing database..."
mysql -h ac-database -u root -p"$DB_ROOT_PASSWORD" <<SQL
CREATE SCHEMA IF NOT EXISTS acore_custom;

CREATE TABLE IF NOT EXISTS acore_custom.account_reset_request (
  uuid VARCHAR(255) NOT NULL,
  created_at datetime,
  email VARCHAR(255),
  ip_address VARCHAR(255),
  valid_request TINYINT,
  PRIMARY KEY (uuid)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS acore_custom.store_account_tokens (
  uuid VARCHAR(255) NOT NULL,
  free_token integer,
  premium_token integer,
  vote_token integer,
  account_id INT,
  PRIMARY KEY (uuid)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS acore_custom.store_package_availability (
  uuid VARCHAR(255) NOT NULL,
  current_price bigint,
  current_price_units VARCHAR(255),
  ends_at datetime,
  starts_at datetime,
  item_base VARCHAR(255),
  PRIMARY KEY (uuid)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS acore_custom.store_package_base (
  uuid VARCHAR(255) NOT NULL,
  copper integer,
  full_price bigint,
  image_url VARCHAR(255),
  name_package VARCHAR(255),
  price_units VARCHAR(255),
  subtext VARCHAR(255),
  type VARCHAR(255),
  PRIMARY KEY (uuid)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS acore_custom.store_package_item (
  uuid VARCHAR(255) NOT NULL,
  item_id_alliance integer,
  item_id_horde integer,
  quantity_alliance integer,
  quantity_horde integer,
  item_base_uuid VARCHAR(255),
  PRIMARY KEY (uuid)
) ENGINE=InnoDB;

CREATE USER IF NOT EXISTS 'spring'@'%' IDENTIFIED BY '$SPRING_DATASOURCE_PASSWORD';
GRANT ALL PRIVILEGES ON acore_custom.* TO 'spring'@'%';
GRANT ALL PRIVILEGES ON acore_auth.* TO 'spring'@'%';
GRANT ALL PRIVILEGES ON acore_characters.* TO 'spring'@'%';
GRANT ALL PRIVILEGES ON acore_world.* TO 'spring'@'%';
FLUSH PRIVILEGES;
SQL

echo "Starting API..."
exec java -jar /app/app.jar