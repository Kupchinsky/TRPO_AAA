ALTER TABLE `accounting_resources`
	DROP COLUMN `resource_id`,
	DROP COLUMN `role`,
	DROP INDEX `U_accounting_resource`,
	DROP FOREIGN KEY `accounting_resources_ibfk_2`,
	ADD COLUMN `resources_users_id` INT(11) NOT NULL AFTER `accounting_id`,
	ADD UNIQUE INDEX `U_accounting_resource` (`accounting_id`, `resources_users_id`),
	ADD FOREIGN KEY (`resources_users_id`) REFERENCES `resources_users` (`id`) ON DELETE CASCADE;
