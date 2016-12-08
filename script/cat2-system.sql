CREATE TABLE `system_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '系统名称，如client, service',
  `format` int(4) COMMENT '系统配置格式，1/xml',
  `version` int(4) COMMENT '系统配置版本号',
  `content` longtext COMMENT '系统配置内容',
  `creation_date` datetime NOT NULL COMMENT '创建时间',
  `last_modified_date` datetime NOT NULL COMMENT '上次修改时间',
  `last_modified_by` varchar(50) DEFAULT NULL COMMENT '上次修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UIDX_Name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='系统配置表';
