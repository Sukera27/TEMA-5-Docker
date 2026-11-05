-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema simpsonsaudit
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `simpsonsaudit` ;

-- -----------------------------------------------------
-- Schema simpsonsaudit
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `simpsonsaudit` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `simpsonsaudit` ;

-- -----------------------------------------------------
-- Table `simpsonsaudit`.`simpsons`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `simpsonsaudit`.`simpsons` ;

CREATE TABLE IF NOT EXISTS `simpsonsaudit`.`simpsons` (
  `simpsons_date` DATETIME(6) NULL DEFAULT NULL,
  `simpsons_id` BIGINT NOT NULL AUTO_INCREMENT,
  `simpsons_name` VARCHAR(255) NULL DEFAULT NULL,
  `simpsons_pic_url` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`simpsons_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
