USE nevis_dev;

LOCK TABLES
nevis_authorities WRITE,
nevis_countries WRITE,
nevis_account_access WRITE,
nevis_account_email WRITE,
nevis_account_phone WRITE,
nevis_account_personal WRITE,
nevis_account_authority WRITE;

-- Password for account email 'test1@dwfe.ru' = test11
-- Password for account email 'test2@dwfe.ru' = test22
INSERT INTO nevis_authorities VALUES
  ('ADMIN', 'Administrator'),
  ('USER', 'Standard user');
INSERT INTO nevis_countries VALUES
  ('Russia', 'RU', 'RUS', '7'),
  ('Ukraine', 'UA', 'UKR', '380'),
  ('Germany', 'DE', 'DEU', '49'),
  ('United States', 'US', 'USA', '1'),
  ('United Kingdom', 'GB', 'GBR', '44'),
  ('Japan', 'JP', 'JPN', '81');
INSERT INTO nevis_account_access VALUES
  (1000, '{bcrypt}$2a$10$cWUX5MiFl8rJFVxKxEbON.2QcJ/0RsVfhVvvqDG5wEOM/bstMIk6m', 1, 1, 1, 1, '2017-07-07 07:07:07', '2017-07-07 07:07:07'),
  (1001, '{bcrypt}$2a$10$9SCLBifjy2Ieaoc6VLmSgOQsxf4NUlbGO32zMraftTXcl3jEAqlbm', 1, 1, 1, 1, '2017-07-07 07:07:07', '2017-07-07 07:07:07');
INSERT INTO nevis_account_email VALUES
  (1000, 'test1@dwfe.ru', 0, 1, '2017-07-07 07:07:07'),
  (1001, 'test2@dwfe.ru', 1, 1, '2017-07-07 07:07:07');
INSERT INTO nevis_account_phone VALUES
  (1000, '+79990011273', 0, 1, '2017-07-07 07:07:07'),
  (1001, '+79094141719', 1, 1, '2017-07-07 07:07:07');
INSERT INTO nevis_account_personal VALUES
  (1000, 'test1', 0, null, 0, null, 0, null, 0, null, 0, null, 0, null, 0, null, 0, null, 0, null, 0, '2017-07-07 07:07:07'),
  (1001, 'test2', 1, null, 1, null, 1, null, 1, null, 1, null, 1, null, 1, null, 1, null, 1, null, 1, '2017-07-07 07:07:07');
INSERT INTO nevis_account_authority VALUES
  (1000, 'ADMIN'),
  (1000, 'USER'),
  (1001, 'USER');

UNLOCK TABLES;