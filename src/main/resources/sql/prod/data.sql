USE nevis_prod;

LOCK TABLES
nevis_authorities WRITE,
nevis_countries WRITE;

INSERT INTO nevis_authorities
VALUES ('ADMIN', 'Administrator'),
       ('USER', 'Standard user');
INSERT INTO nevis_countries
VALUES ('Russia', 'RU', 'RUS', '7'),
       ('Ukraine', 'UA', 'UKR', '380'),
       ('Germany', 'DE', 'DEU', '49'),
       ('United States', 'US', 'USA', '1'),
       ('United Kingdom', 'GB', 'GBR', '44'),
       ('Japan', 'JP', 'JPN', '81');

UNLOCK TABLES;
