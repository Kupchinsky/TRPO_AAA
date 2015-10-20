CREATE TABLE IF NOT EXISTS `users` (
	`id` int(11) NOT NULL AUTO_INCREMENT,
	`login` varchar(32) NOT NULL,
	`passwordHash` varchar(64) NOT NULL,
	`salt` varchar(32) NOT NULL,
	`personName` varchar(64) NOT NULL,
	PRIMARY KEY (`id`),
	UNIQUE KEY `login` (`login`)
) DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `accounting` (
	`id` int(11) NOT NULL AUTO_INCREMENT,
	`user_id` int(11) NOT NULL,
	`role` tinyint(4) NOT NULL,
	`volume` int(11) NOT NULL DEFAULT '0',
	`logon_date` timestamp NOT NULL,
	`logout_date` timestamp NOT NULL,
	PRIMARY KEY (`id`),
	FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `resources` (
	`id` int(11) NOT NULL AUTO_INCREMENT,
	`name` varchar(64) NOT NULL,
	`parent_resource_id` int(11) DEFAULT NULL,
	PRIMARY KEY (`id`),
	UNIQUE KEY `name` (`name`),
	FOREIGN KEY (`parent_resource_id`) REFERENCES `resources` (`id`) ON DELETE CASCADE
) DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `resources_users` (
	`id` int(11) NOT NULL AUTO_INCREMENT,
	`resource_id` int(11) NOT NULL,
	`user_id` int(11) NOT NULL,
	`role` tinyint(4) NOT NULL,
	PRIMARY KEY (`id`),
	UNIQUE KEY `U_resource_user_role` (`resource_id`,`user_id`,`role`),
	FOREIGN KEY (`resource_id`) REFERENCES `resources` (`id`) ON DELETE CASCADE,
	FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `accounting_resources` (
	`id` int(11) NOT NULL AUTO_INCREMENT,
	`accounting_id` int(11) NOT NULL,
	`resource_id` int(11) NOT NULL,
	PRIMARY KEY (`id`),
	UNIQUE KEY `U_accounting_resource` (`accounting_id`,`resource_id`),
	FOREIGN KEY (`accounting_id`) REFERENCES `accounting` (`id`) ON DELETE CASCADE,
	FOREIGN KEY (`resource_id`) REFERENCES `resources` (`id`) ON DELETE CASCADE
) DEFAULT CHARSET=utf8;