USE dwfe_prod;

LOCK TABLES
dwfe_countries WRITE,
nevis_authorities WRITE;


--
-- DWFE
--

INSERT INTO dwfe_countries
VALUES ('Russia', 'RU', 'RUS', '7'),
       ('Ukraine', 'UA', 'UKR', '380'),
       ('Germany', 'DE', 'DEU', '49'),
       ('United States', 'US', 'USA', '1'),
       ('United Kingdom', 'GB', 'GBR', '44'),
       ('Japan', 'JP', 'JPN', '81');


--
-- MODULE: Nevis
--

INSERT INTO nevis_authorities
VALUES ('ADMIN', 'Administrator'),
       ('USER', 'Standard user');


UNLOCK TABLES;
