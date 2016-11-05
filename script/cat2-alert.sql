CREATE TABLE `alert_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '名称，如rule, recipient',
  `format` int(4) COMMENT '报表格式，1/xml',
  `version` int(4) COMMENT '报表配置版本号',
  `content` longtext COMMENT '报表配置内容',
  `creation_date` datetime NOT NULL COMMENT '创建时间',
  `last_modified_date` datetime NOT NULL COMMENT '上次修改时间',
  `last_modified_by` varchar(50) DEFAULT NULL COMMENT '上次修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UIDX_Name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='告警配置表';
