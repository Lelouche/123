DROP SCHEMA IF EXISTS fitbit_crypt;
CREATE SCHEMA IF NOT EXISTS `fitbit_crypt` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `fitbit_crypt`;

-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `fitbit_crypt`.`heart_rate` (
  `clientId` CHAR(6) not null,
  `date`     VARCHAR(50) not null,
  `time`     VARCHAR(50) not null,
  `level`    int not null,
  `value`    TINYINT unsigned not null
);