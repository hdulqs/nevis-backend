USE dwfe_dev;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS
dwfe_mailing,
nevis_account_access,
nevis_account_email,
nevis_account_phone,
nevis_authorities,
nevis_account_authority,
nevis_countries,
nevis_account_personal,
oauth_access_token,
oauth_refresh_token;
SET FOREIGN_KEY_CHECKS = 1;

--
-- DWFE
--

CREATE TABLE dwfe_mailing (
  created_on            DATETIME                                                                                                     NOT NULL               DEFAULT CURRENT_TIMESTAMP,
  `type`                ENUM ('WELCOME_ONLY', 'WELCOME_PASSWORD', 'PASSWORD_WAS_CHANGED', 'PASSWORD_RESET_CONFIRM', 'EMAIL_CONFIRM') NOT NULL,
  email                 VARCHAR(50)                                                                                                  NOT NULL,
  sent                  TINYINT(1)                                                                                                   NOT NULL,
  max_attempts_reached  TINYINT(1)                                                                                                   NOT NULL,
  data                  VARCHAR(2000)                                                                                                NOT NULL               DEFAULT '',
  cause_of_last_failure VARCHAR(2000),
  updated_on            DATETIME ON UPDATE CURRENT_TIMESTAMP                                                                                                DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (created_on, `type`, email)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

--
-- MODULE: Nevis
--
CREATE TABLE nevis_account_access (
  id                      BIGINT(20)                           NOT NULL   AUTO_INCREMENT,
  password                VARCHAR(100)                         NOT NULL,
  third_party             ENUM ('GOOGLE', 'FACEBOOK'),
  account_non_expired     TINYINT(1)                           NOT NULL   DEFAULT '1',
  credentials_non_expired TINYINT(1)                           NOT NULL   DEFAULT '1',
  account_non_locked      TINYINT(1)                           NOT NULL   DEFAULT '1',
  enabled                 TINYINT(1)                           NOT NULL   DEFAULT '1',
  created_on              DATETIME                             NOT NULL   DEFAULT CURRENT_TIMESTAMP,
  updated_on              DATETIME ON UPDATE CURRENT_TIMESTAMP NOT NULL   DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
)
  AUTO_INCREMENT = 1000
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


CREATE TABLE nevis_account_email (
  account_id BIGINT(20)                           NOT NULL,
  value      VARCHAR(100)                         NOT NULL,
  non_public TINYINT(1)                           NOT NULL   DEFAULT '1',
  confirmed  TINYINT(1)                           NOT NULL   DEFAULT '0',
  updated_on DATETIME ON UPDATE CURRENT_TIMESTAMP NOT NULL   DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY nevis_account_email_value_uindex (value),
  KEY nevis_account_email_account_id_fk (account_id),
  CONSTRAINT nevis_account_email_account_id_fk FOREIGN KEY (account_id) REFERENCES nevis_account_access (id)
    ON DELETE CASCADE
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


CREATE TABLE nevis_account_phone (
  account_id BIGINT(20)                           NOT NULL,
  value      VARCHAR(100)                         NOT NULL,
  non_public TINYINT(1)                           NOT NULL   DEFAULT '1',
  confirmed  TINYINT(1)                           NOT NULL   DEFAULT '0',
  updated_on DATETIME ON UPDATE CURRENT_TIMESTAMP NOT NULL   DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY nevis_account_phone_value_uindex (value),
  KEY nevis_account_phone_account_id_fk (account_id),
  CONSTRAINT nevis_account_phone_account_id_fk FOREIGN KEY (account_id) REFERENCES nevis_account_access (id)
    ON DELETE CASCADE
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


CREATE TABLE nevis_authorities (
  authority   VARCHAR(20)  NOT NULL,
  description VARCHAR(100) NOT NULL DEFAULT '',
  PRIMARY KEY (authority)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


CREATE TABLE nevis_account_authority (
  account_id BIGINT(20)  NOT NULL,
  authority  VARCHAR(20) NOT NULL,
  KEY nevis_account_authority_access_id_fk (account_id),
  KEY nevis_account_authority_authorities_authority_fk (authority),
  CONSTRAINT nevis_account_authority_access_id_fk FOREIGN KEY (account_id) REFERENCES nevis_account_access (id)
    ON DELETE CASCADE,
  CONSTRAINT nevis_account_authority_authorities_authority_fk FOREIGN KEY (authority) REFERENCES nevis_authorities (authority)
    ON DELETE CASCADE
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


CREATE TABLE nevis_countries (
  country    VARCHAR(100) NOT NULL,
  alpha2     VARCHAR(2)   NOT NULL,
  alpha3     VARCHAR(3)   NOT NULL DEFAULT '',
  phone_code VARCHAR(50),
  PRIMARY KEY (alpha2)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


CREATE TABLE nevis_account_personal (
  account_id               BIGINT(20)                           NOT NULL,
  nick_name                VARCHAR(100),
  nick_name_non_public     TINYINT(1)                           NOT NULL   DEFAULT '1',
  first_name               VARCHAR(20),
  first_name_non_public    TINYINT(1)                           NOT NULL   DEFAULT '1',
  middle_name              VARCHAR(20),
  middle_name_non_public   TINYINT(1)                           NOT NULL   DEFAULT '1',
  last_name                VARCHAR(20),
  last_name_non_public     TINYINT(1)                           NOT NULL   DEFAULT '1',
  gender                   ENUM ('M', 'F'),
  gender_non_public        TINYINT(1)                           NOT NULL   DEFAULT '1',
  date_of_birth            DATE,
  date_of_birth_non_public TINYINT(1)                           NOT NULL   DEFAULT '1',
  country                  VARCHAR(2),
  country_non_public       TINYINT(1)                           NOT NULL   DEFAULT '1',
  city                     VARCHAR(100),
  city_non_public          TINYINT(1)                           NOT NULL   DEFAULT '1',
  company                  VARCHAR(100),
  company_non_public       TINYINT(1)                           NOT NULL   DEFAULT '1',
  position_held            VARCHAR(100),
  position_held_non_public TINYINT(1)                           NOT NULL   DEFAULT '1',
  updated_on               DATETIME ON UPDATE CURRENT_TIMESTAMP NOT NULL   DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY nevis_account_personal_nick_name_uindex (nick_name),
  KEY nevis_account_personal_account_id_fk (account_id),
  CONSTRAINT nevis_account_personal_account_id_fk FOREIGN KEY (account_id) REFERENCES nevis_account_access (id)
    ON DELETE CASCADE,
  CONSTRAINT nevis_account_personal_countries_country_fk FOREIGN KEY (country) REFERENCES nevis_countries (alpha2)
    ON DELETE CASCADE
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

--
-- To persist tokens between server restarts I need:
-- 1) to configure a persistent token store (JdbcTokenStore for example, see config/TokenStoreConfig.java)
-- 2) create SQL tables: https://github.com/spring-projects/spring-security-oauth/blob/master/spring-security-oauth2/src/test/resources/schema.sql
--    (minimum):
CREATE TABLE oauth_access_token (
  token_id          VARCHAR(256),
  token             BLOB,
  authentication_id VARCHAR(256) PRIMARY KEY,
  user_name         VARCHAR(256),
  client_id         VARCHAR(256),
  authentication    BLOB,
  refresh_token     VARCHAR(256)
);

CREATE TABLE oauth_refresh_token (
  token_id       VARCHAR(256),
  token          BLOB,
  authentication BLOB
);