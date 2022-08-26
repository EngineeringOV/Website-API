CREATE SCHEMA acore_world;
CREATE SCHEMA acore_characters;
CREATE SCHEMA acore_auth;
CREATE SCHEMA acore_custom;

CREATE TABLE acore_custom.account_reset_request (
    `uuid`        CHAR(36)    primary key,
    email         VARCHAR(50)    NOT NULL,
    ip_address    VARCHAR(15)    NOT NULL,
    valid_request TINYINT(1)     NOT NULL,
    created_at    timestamp      DEFAULT CURRENT_TIMESTAMP
);

use acore_custom;