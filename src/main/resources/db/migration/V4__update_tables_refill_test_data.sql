ALTER TABLE `accounting_resources` ADD COLUMN `role` TINYINT(4) NOT NULL AFTER `resource_id`;

INSERT INTO `resources_users` (`id`, `resource_id`, `user_id`, `role`) VALUES
	(5, 2, 1, 1);
