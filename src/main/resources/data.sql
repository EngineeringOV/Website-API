CREATE SCHEMA acore_world;
CREATE SCHEMA acore_characters;
CREATE SCHEMA acore_auth;
CREATE SCHEMA acore_custom;

CREATE TABLE acore_custom.account_reset_request (
    uuid          VARCHAR(30)    primary key,
    email         VARCHAR(50)    NOT NULL,
    ip_address    VARCHAR(15)    NOT NULL,
    valid_request TINYINT(1)     NOT NULL,
    created_at    timestamp      current_timestamp
);

use acore_custom;