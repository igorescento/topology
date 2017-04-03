CREATE TABLE `lsa` (
  `id` bigint(11) NOT NULL,
  `instance` varchar(255) NOT NULL,
  `area` varchar(255) DEFAULT NULL,
  `type` varchar(255) NOT NULL,
  `originator` bigint(11) NOT NULL,
  `sequence` varchar(255) NOT NULL,
  `age` int(10) NOT NULL,
  `checksum` varchar(45) NOT NULL,
  `options` varchar(10) NOT NULL,
  `body` text,
  PRIMARY KEY (`id`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='OSPF LSA table from Mikrotik router.';