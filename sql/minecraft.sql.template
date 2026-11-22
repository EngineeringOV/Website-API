-- Minecraft accounts on top of AzerothCore accounts. Only used with minecraft.auth.enabled=true.
-- Run automatically at startup when enabled; also in sql/minecraft.sql.template.

CREATE TABLE IF NOT EXISTS acore_custom.mc_profile (
  id CHAR(32) NOT NULL,
  account_id INT UNSIGNED NOT NULL,
  name VARCHAR(16) NOT NULL,
  name_key VARCHAR(16) NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_mc_profile_account (account_id),
  UNIQUE KEY uk_mc_profile_name (name_key)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS acore_custom.mc_token (
  token_hash CHAR(64) NOT NULL,
  client_token VARCHAR(64) NOT NULL,
  account_id INT UNSIGNED NOT NULL,
  expires_at DATETIME NOT NULL,
  PRIMARY KEY (token_hash),
  KEY ix_mc_token_account (account_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS acore_custom.mc_name_clash (
  name_key VARCHAR(16) NOT NULL,
  mojang_id CHAR(32) NOT NULL,
  name VARCHAR(16) NOT NULL,
  suffix INT NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (name_key, mojang_id),
  UNIQUE KEY uk_mc_name_clash_suffix (name_key, suffix)
) ENGINE=InnoDB;
