CREATE TABLE `hourly_report` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(30) NOT NULL COMMENT '报表名称，如：transaction,event,problem等',
  `ip` varchar(20) NOT NULL COMMENT '报表来自于哪台cat-consumer机器，如：192.168.1.1',
  `domain` varchar(50) NOT NULL COMMENT '应用项目名称,如：Cat',
  `index` int(11) NOT NULL COMMENT '同一IP下第几个报表',
  `start_time` timestamp NOT NULL COMMENT '报表开始时间,如：2016-07-03 10:00:00',
  `creation_date` timestamp NOT NULL COMMENT '报表创建时间,如：2016-07-03 10:34:00',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UIDX_StartTime_Domain_Name_Index` (`start_time`,`domain`,`name`,`index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='用于存放实时小时报表信息';

CREATE TABLE `hourly_report_content` (
  `report_id` int(11) NOT NULL COMMENT '报表ID',
  `format` tinyint(4) NOT NULL COMMENT '报表类型, 11/二进制+gzip压缩， 12/二进制+snappy压缩',
  `content` longblob NOT NULL COMMENT '报表内容',
  `creation_date` timestamp NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='用于存放实时小时报表内容';

CREATE TABLE `history_report` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` int(4) NOT NULL COMMENT '报表类型，1/daily, 2/weekly, 3/monthly, 4/yearly',
  `name` varchar(30) NOT NULL COMMENT '报表名称，如：transaction,event,problem等',
  `ip` varchar(20) NOT NULL COMMENT '报表来自于哪台cat-consumer机器，如：192.168.1.1',
  `domain` varchar(50) NOT NULL COMMENT '应用项目名称,如：Cat',
  `start_time` timestamp NOT NULL COMMENT '报表开始时间,如：2016-07-03 00:00:00',
  `creation_date` datetime NOT NULL COMMENT '创建时间,如：2016-07-03 10:34:00',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UIDX_StartTime_Domain_Name` (`start_time`,`domain`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用于存放实时历史报表信息';

CREATE TABLE `history_report_content` (
  `report_id` int(11) NOT NULL COMMENT '报表ID',
  `format` tinyint(4) NOT NULL COMMENT '报表类型, 11/二进制+gzip压缩， 12/二进制+snappy压缩',
  `content` longblob NOT NULL COMMENT '报表内容',
  `creation_date` timestamp NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='历史报表内容';

CREATE TABLE `report_task` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `task_type` int(4) NOT NULL COMMENT '任务类型，1/daily, 2/weekly, 3/monthly, 4/yearly',
  `report_name` varchar(30) NOT NULL COMMENT '报表名称，如：transaction,event,problem等',
  `report_start_time` datetime NOT NULL COMMENT '报表开始时间',
  `schedule_time` datetime NOT NULL COMMENT '任务计划时间',
  `status` tinyint(4) NOT NULL COMMENT '执行状态: 1/todo, 2/doing, 3/done, 4/failed',  
  `producer_ip` varchar(20) NOT NULL COMMENT '任务创建者ip',
  `creation_date` datetime NOT NULL COMMENT '创建时间',
  `last_modified_date` datetime NOT NULL COMMENT '上次修改时间',
  `failure_count` tinyint(4) NOT NULL COMMENT '任务失败次数',
  `failure_reason` varchar(2000) DEFAULT NULL COMMENT '任务失败原因',  
  `expected_done_date` datetime DEFAULT NULL COMMENT '任务预计完成时间',
  `consumer_ip` varchar(20) DEFAULT NULL COMMENT '任务执行者ip',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UIDX_StartTime_Name_Type` (`report_start_time`,`report_name`,`task_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='后台任务';
