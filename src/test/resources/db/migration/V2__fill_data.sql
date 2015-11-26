INSERT INTO `users` (`id`, `login`, `passwordHash`, `salt`, `personName`) VALUES
	(1, 'jdoe', '9433735ce5af6e1fc41b7182457c377ab6b65975', 'wjw6YPcIs5', 'John Doe'),
	(2, 'jrow', 'd50cb8c6a59ec55af6d46e27a640775af6098128', 'ROyqeKuULB', 'Jane Row');

INSERT INTO `resources` (`id`, `name`, `parent_resource_id`) VALUES
	(1, 'a', NULL),
	(2, 'a.b', 1),
	(3, 'a.b.c', 2),
	(4, 'a.bc', 1);

INSERT INTO `resources_users` (`id`, `resource_id`, `user_id`, `role`) VALUES
	(1, 1, 1, 0),
	(2, 2, 1, 1),
	(3, 3, 2, 2),
	(4, 4, 1, 2);
