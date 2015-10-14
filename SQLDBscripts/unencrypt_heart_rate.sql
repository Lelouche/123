DROP SCHEMA IF EXISTS fitbit_simple;
CREATE SCHEMA IF NOT EXISTS `fitbit_simple` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `fitbit_simple`;

-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `fitbit_simple`.`heart_rate` (
  `clientId` CHAR(6) not null,
  `date`     VARCHAR(50) not null,
  `time`     VARCHAR(50) not null,
  `level`    int not null,
  `value`    TINYINT unsigned not null
);