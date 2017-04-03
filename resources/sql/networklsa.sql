CREATE TABLE `networklsa` (
  `id` bigint(11) NOT NULL,
  `type` varchar(255) NOT NULL,
  `netmask` bigint(11) NOT NULL,
  `routersid` varchar(3000) NOT NULL,
  `numrouters` int(11) NOT NULL,
  `originator` bigint(11) NOT NULL,
  `firstaddr` bigint(11) NOT NULL,
  `lastaddr` bigint(11) NOT NULL,
  `networkaddr` bigint(11) NOT NULL,
  `broadcastaddr` bigint(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;