CREATE TABLE `routerlsa` (
  `id` bigint(11) NOT NULL,
  `type` varchar(255) NOT NULL,
  `linktype` varchar(255) NOT NULL,
  `bodyid` bigint(11) NOT NULL,
  `data` bigint(11) NOT NULL,
  `metric` int(11) NOT NULL,
  `seq` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`seq`),
  UNIQUE KEY `seq_UNIQUE` (`seq`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;