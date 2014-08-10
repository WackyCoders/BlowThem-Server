CREATE TABLE `tanks` (
  `name` text,
  `description` text,
  `first_weapon` int(11) DEFAULT NULL,
  `second_weapon` int(11) DEFAULT NULL,
  `armor` int(11) DEFAULT NULL,
  `engine` int(11) DEFAULT NULL,
  `cost` int(11) DEFAULT NULL,
  `id_tank` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id_tank`));

CREATE TABLE `armor` (
  `tank` int(11) DEFAULT NULL,
  `name` text,
  `description` text,
  `armor` int(11) DEFAULT NULL,
  `cost` int(11) DEFAULT NULL,
  `id_armor` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id_armor`),
  KEY `armor_tank` (`tank`),
  CONSTRAINT `armor_tank` FOREIGN KEY (`tank`) REFERENCES `tanks` (`id_tank`)
);
CREATE TABLE `engine` (
  `tank` int(11) DEFAULT NULL,
  `name` text,
  `description` text,
  `speed` int(11) DEFAULT NULL,
  `cost` int(11) DEFAULT NULL,
  `id_engine` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id_engine`),
  KEY `engine_tank` (`tank`),
  CONSTRAINT `engine_tank` FOREIGN KEY (`tank`) REFERENCES `tanks` (`id_tank`)
);
CREATE TABLE `first_weapon` (
  `tank` int(11) DEFAULT NULL,
  `name` text,
  `description` text,
  `atack` int(11) DEFAULT NULL,
  `cost` int(11) DEFAULT NULL,
  `id_weapon` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id_weapon`),
  KEY `first_weapon_tank` (`tank`),
  CONSTRAINT `first_weapon_tank` FOREIGN KEY (`tank`) REFERENCES `tanks` (`id_tank`)
);
CREATE TABLE `second_weapon` (
  `tank` int(11) DEFAULT NULL,
  `name` text,
  `description` text,
  `atack` int(11) DEFAULT NULL,
  `cost` int(11) DEFAULT NULL,
  `id_weapon` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id_weapon`),
  KEY `second_weapon_tank` (`tank`),
  CONSTRAINT `second_weapon_tank` FOREIGN KEY (`tank`) REFERENCES `tanks` (`id_tank`)
);

ALTER TABLE tanks ADD CONSTRAINT `to_armor_f1` FOREIGN KEY (`armor`) REFERENCES `armor` (`id_armor`),
ADD CONSTRAINT `to_engine_f1` FOREIGN KEY (`engine`) REFERENCES `engine` (`id_engine`),
ADD CONSTRAINT `to_first_weapon_f1` FOREIGN KEY (`first_weapon`) REFERENCES `first_weapon` (`id_weapon`),
ADD CONSTRAINT `to_second_weapon_f1` FOREIGN KEY (`second_weapon`) REFERENCES `second_weapon` (`id_weapon`);

CREATE TABLE `users` (
  `username` varchar(64) NOT NULL,
  `password` varchar(128) NOT NULL,
  `mail` varchar(320) CHARACTER SET ascii NOT NULL,
  `scores` int(11) DEFAULT '0',
  `money` int(11) DEFAULT '0',
  `tank` int(11) DEFAULT NULL,
  `id_user` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id_user`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `mail` (`mail`));

CREATE TABLE `garage` (
  `user` int(11) DEFAULT NULL,
  `tank` int(11) DEFAULT NULL,
  `first_weapon` int(11) DEFAULT NULL,
  `second_weapon` int(11) DEFAULT NULL,
  `armor` int(11) DEFAULT NULL,
  `engine` int(11) DEFAULT NULL,
  `id_tank` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id_tank`),
  KEY `to_tank` (`tank`),
  KEY `to_first_weapon` (`first_weapon`),
  KEY `to_second_weapon` (`second_weapon`),
  KEY `to_armor` (`armor`),
  KEY `to_engine` (`engine`),
  KEY `user` (`user`),
  CONSTRAINT `garage_ibfk_1` FOREIGN KEY (`user`) REFERENCES `users` (`id_user`) ON DELETE CASCADE,
  CONSTRAINT `to_armor` FOREIGN KEY (`armor`) REFERENCES `armor` (`id_armor`),
  CONSTRAINT `to_engine` FOREIGN KEY (`engine`) REFERENCES `engine` (`id_engine`),
  CONSTRAINT `to_first_weapon` FOREIGN KEY (`first_weapon`) REFERENCES `first_weapon` (`id_weapon`),
  CONSTRAINT `to_second_weapon` FOREIGN KEY (`second_weapon`) REFERENCES `second_weapon` (`id_weapon`),
  CONSTRAINT `to_tank` FOREIGN KEY (`tank`) REFERENCES `tanks` (`id_tank`)
);

ALTER TABLE users ADD CONSTRAINT `users_ibfk_1` FOREIGN KEY (`tank`) REFERENCES `garage` (`id_tank`) ON DELETE SET NULL;

CREATE TABLE `garage_armor` (
  `user` int(11) DEFAULT NULL,
  `armor` int(11) DEFAULT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  KEY `armor` (`armor`),
  KEY `user` (`user`),
  CONSTRAINT `garage_armor_ibfk_1` FOREIGN KEY (`armor`) REFERENCES `armor` (`id_armor`) ON DELETE SET NULL,
  CONSTRAINT `garage_armor_ibfk_2` FOREIGN KEY (`user`) REFERENCES `users` (`id_user`) ON DELETE CASCADE
);
CREATE TABLE `garage_engine` (
  `user` int(11) DEFAULT NULL,
  `engine` int(11) DEFAULT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  KEY `engine` (`engine`),
  CONSTRAINT `garage_engine_ibfk_1` FOREIGN KEY (`engine`) REFERENCES `engine` (`id_engine`) ON DELETE SET NULL,
  KEY `user` (`user`),
  CONSTRAINT `garage_engine_ibfk_2` FOREIGN KEY (`user`) REFERENCES `users` (`id_user`) ON DELETE Cascade
);
CREATE TABLE `garage_first_weapon` (
  `user` int(11) DEFAULT NULL,
  `first_weapon` int(11) DEFAULT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  KEY `first_weapon` (`first_weapon`),
  CONSTRAINT `garage_first_weapon_ibfk_1` FOREIGN KEY (`first_weapon`) REFERENCES `first_weapon` (`id_weapon`) ON DELETE SET NULL,
  KEY `user` (`user`),
  CONSTRAINT `garage_first_weapon_ibfk_2` FOREIGN KEY (`user`) REFERENCES `users` (`id_user`) ON DELETE Cascade
) ;
CREATE TABLE `garage_second_weapon` (
  `user` int(11) DEFAULT NULL,
  `second_weapon` int(11) DEFAULT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  KEY `second_weapon` (`second_weapon`),
  CONSTRAINT `garage_second_weapon_ibfk_1` FOREIGN KEY (`second_weapon`) REFERENCES `second_weapon` (`id_weapon`) ON DELETE SET NULL,
  KEY `user` (`user`),
  CONSTRAINT `garage_second_weapon_ibfk_2` FOREIGN KEY (`user`) REFERENCES `users` (`id_user`) ON DELETE Cascade
);

alter table garage add unique (user, tank);
alter table garage_armor add unique (user, armor);
alter table garage_engine add unique (user, engine);
alter table garage_first_weapon add unique (user, first_weapon);
alter table garage_second_weapon add unique (user, second_weapon);