drop table if exists `station`;
create table `station` (
                           `id` bigint not null comment 'id',
                           `name` varchar(20) not null comment '站名',
                           `name_pinyin` varchar(50) not null comment '站名拼音',
                           `name_py` varchar(50) not null comment '站名拼音首字母',
                           `create_time` datetime(3) comment '新增时间',
                           `update_time` datetime(3) comment '修改时间',
                           primary key (`id`),
                           unique key `name_unique` (`name`)
) engine=innodb default charset=utf8mb4 comment='车站';

drop table if exists `train`;
create table `train` (
                         `id` bigint not null comment 'id',
                         `code` varchar(20) not null comment '车次编号',
                         `type` char(1) not null comment '车次类型|枚举[TrainTypeEnum]',
                         `start` varchar(20) not null comment '始发站',
                         `start_pinyin` varchar(50) not null comment '始发站拼音',
                         `start_time` time not null comment '出发时间',
                         `end` varchar(20) not null comment '终点站',
                         `end_pinyin` varchar(50) not null comment '终点站拼音',
                         `end_time` time not null comment '到站时间',
                         `create_time` datetime(3) comment '新增时间',
                         `update_time` datetime(3) comment '修改时间',
                         primary key (`id`),
                         unique key `code_unique` (`code`)
) engine=innodb default charset=utf8mb4 comment='车次';

drop table if exists `train_station`;
create table `train_station` (
                                 `id` bigint not null comment 'id',
                                 `train_code` varchar(20) not null comment '车次编号',
                                 `index` int not null comment '站序',
                                 `name` varchar(20) not null comment '站名',
                                 `name_pinyin` varchar(50) not null comment '站名拼音',
                                 `in_time` time comment '进站时间',
                                 `out_time` time comment '出站时间',
                                 `stop_time` time comment '停站时长',
                                 `km` decimal(8, 2) not null comment '里程（公里）|从上一站到本站的距离',
                                 `create_time` datetime(3) comment '新增时间',
                                 `update_time` datetime(3) comment '修改时间',
                                 primary key (`id`),
                                 unique key `train_code_index_unique` (`train_code`, `index`),
                                 unique key `train_code_name_unique` (`train_code`, `name`)
) engine=innodb default charset=utf8mb4 comment='火车车站';

drop table if exists `train_carriage`;
create table `train_carriage` (
                                  `id` bigint not null comment 'id',
                                  `train_code` varchar(20) not null comment '车次编号',
                                  `index` int not null comment '厢号',
                                  `seat_type` char(1) not null comment '座位类型|枚举[SeatTypeEnum]',
                                  `seat_count` int not null comment '座位数',
                                  `row_count` int not null comment '排数',
                                  `col_count` int not null comment '列数',
                                  `create_time` datetime(3) comment '新增时间',
                                  `update_time` datetime(3) comment '修改时间',
                                  unique key `train_code_index_unique` (`train_code`, `index`),
                                  primary key (`id`)
) engine=innodb default charset=utf8mb4 comment='火车车厢';

drop table if exists `train_seat`;
create table `train_seat` (
                              `id` bigint not null comment 'id',
                              `train_code` varchar(20) not null comment '车次编号',
                              `carriage_index` int not null comment '厢序',
                              `row` char(2) not null comment '排号|01, 02',
                              `col` char(1) not null comment '列号|枚举[SeatColEnum]',
                              `seat_type` char(1) not null comment '座位类型|枚举[SeatTypeEnum]',
                              `carriage_seat_index` int not null comment '同车厢座序',
                              `create_time` datetime(3) comment '新增时间',
                              `update_time` datetime(3) comment '修改时间',
                              primary key (`id`)
) engine=innodb default charset=utf8mb4 comment='座位';

DROP TABLE IF EXISTS `daily_train`;
CREATE TABLE `daily_train`  (
                                `id` bigint NOT NULL COMMENT 'id',
                                `date` date NOT NULL COMMENT '日期',
                                `code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '车次编号',
                                `type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '车次类型|枚举[TrainTypeEnum]',
                                `start` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '始发站',
                                `start_pinyin` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '始发站拼音',
                                `start_time` time(0) NOT NULL COMMENT '出发时间',
                                `end` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '终点站',
                                `end_pinyin` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '终点站拼音',
                                `end_time` time(0) NOT NULL COMMENT '到站时间',
                                `create_time` datetime(3) NULL DEFAULT NULL COMMENT '新增时间',
                                `update_time` datetime(3) NULL DEFAULT NULL COMMENT '修改时间',
                                PRIMARY KEY (`id`) USING BTREE,
                                UNIQUE INDEX `date_code_unique`(`date`, `code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '每日车次' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `daily_train_station`;
CREATE TABLE `daily_train_station`  (
                                        `id` bigint NOT NULL COMMENT 'id',
                                        `date` date NOT NULL COMMENT '日期',
                                        `train_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '车次编号',
                                        `index` int NOT NULL COMMENT '站序',
                                        `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '站名',
                                        `name_pinyin` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '站名拼音',
                                        `in_time` time(0) NULL DEFAULT NULL COMMENT '进站时间',
                                        `out_time` time(0) NULL DEFAULT NULL COMMENT '出站时间',
                                        `stop_time` time(0) NULL DEFAULT NULL COMMENT '停站时长',
                                        `km` decimal(8, 2) NOT NULL COMMENT '里程（公里）|从上一站到本站的距离',
                                        `create_time` datetime(3) NULL DEFAULT NULL COMMENT '新增时间',
                                        `update_time` datetime(3) NULL DEFAULT NULL COMMENT '修改时间',
                                        PRIMARY KEY (`id`) USING BTREE,
                                        UNIQUE INDEX `date_train_code_index_unique`(`date`, `train_code`, `index`) USING BTREE,
                                        UNIQUE INDEX `date_train_code_name_unique`(`date`, `train_code`, `name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '每日车站' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `daily_train_carriage`;
CREATE TABLE `daily_train_carriage`  (
                                         `id` bigint NOT NULL COMMENT 'id',
                                         `date` date NOT NULL COMMENT '日期',
                                         `train_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '车次编号',
                                         `index` int NOT NULL COMMENT '箱序',
                                         `seat_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '座位类型|枚举[SeatTypeEnum]',
                                         `seat_count` int NOT NULL COMMENT '座位数',
                                         `row_count` int NOT NULL COMMENT '排数',
                                         `col_count` int NOT NULL COMMENT '列数',
                                         `create_time` datetime(3) NULL DEFAULT NULL COMMENT '新增时间',
                                         `update_time` datetime(3) NULL DEFAULT NULL COMMENT '修改时间',
                                         PRIMARY KEY (`id`) USING BTREE,
                                         UNIQUE INDEX `date_train_code_index_unique`(`date`, `train_code`, `index`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '每日车厢' ROW_FORMAT = Dynamic;


drop table if exists `daily_train_seat`;
create table `daily_train_seat` (
                                    `id` bigint not null comment 'id',
                                    `date` date not null comment '日期',
                                    `train_code` varchar(20) not null comment '车次编号',
                                    `carriage_index` int not null comment '箱序',
                                    `row` char(2) not null comment '排号|01, 02',
                                    `col` char(1) not null comment '列号|枚举[SeatColEnum]',
                                    `seat_type` char(1) not null comment '座位类型|枚举[SeatTypeEnum]',
                                    `carriage_seat_index` int not null comment '同车箱座序',
                                    `sell` varchar(50) not null comment '售卖情况|将经过的车站用01拼接，0表示可卖，1表示已卖',
                                    `create_time` datetime(3) comment '新增时间',
                                    `update_time` datetime(3) comment '修改时间',
                                    primary key (`id`)
) engine=innodb default charset=utf8mb4 comment='每日座位';

DROP TABLE IF EXISTS `daily_train_ticket`;
CREATE TABLE `daily_train_ticket`  (
                                       `id` bigint NOT NULL COMMENT 'id',
                                       `date` date NOT NULL COMMENT '日期',
                                       `train_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '车次编号',
                                       `start` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出发站',
                                       `start_pinyin` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出发站拼音',
                                       `start_time` time(0) NOT NULL COMMENT '出发时间',
                                       `start_index` int NOT NULL COMMENT '出发站序|本站是整个车次的第几站',
                                       `end` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '到达站',
                                       `end_pinyin` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '到达站拼音',
                                       `end_time` time(0) NOT NULL COMMENT '到站时间',
                                       `end_index` int NOT NULL COMMENT '到站站序|本站是整个车次的第几站',
                                       `ydz` int NOT NULL COMMENT '一等座余票',
                                       `ydz_price` decimal(8, 2) NOT NULL COMMENT '一等座票价',
                                       `edz` int NOT NULL COMMENT '二等座余票',
                                       `edz_price` decimal(8, 2) NOT NULL COMMENT '二等座票价',
                                       `rw` int NOT NULL COMMENT '软卧余票',
                                       `rw_price` decimal(8, 2) NOT NULL COMMENT '软卧票价',
                                       `yw` int NOT NULL COMMENT '硬卧余票',
                                       `yw_price` decimal(8, 2) NOT NULL COMMENT '硬卧票价',
                                       `create_time` datetime(3) NULL DEFAULT NULL COMMENT '新增时间',
                                       `update_time` datetime(3) NULL DEFAULT NULL COMMENT '修改时间',
                                       PRIMARY KEY (`id`) USING BTREE,
                                       UNIQUE INDEX `date_train_code_start_end_unique`(`date`, `train_code`, `start`, `end`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '余票信息' ROW_FORMAT = Dynamic;

drop table if exists `confirm_order`;
create table `confirm_order` (
                                 `id` bigint not null comment 'id',
                                 `member_id` bigint not null comment '会员id',
                                 `date` date not null comment '日期',
                                 `train_code` varchar(20) not null comment '车次编号',
                                 `start` varchar(20) not null comment '出发站',
                                 `end` varchar(20) not null comment '到达站',
                                 `daily_train_ticket_id` bigint not null comment '余票ID',
                                 `tickets` json not null comment '车票',
                                 `status` char(1) not null comment '订单状态|枚举[ConfirmOrderStatusEnum]',
                                 `create_time` datetime(3) comment '新增时间',
                                 `update_time` datetime(3) comment '修改时间',
                                 primary key (`id`),
                                 index `date_train_code_index` (`date`, `train_code`)
) engine=innodb default charset=utf8mb4 comment='确认订单';

DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log`  (
                             `id` bigint NOT NULL AUTO_INCREMENT,
                             `branch_id` bigint NOT NULL,
                             `xid` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
                             `context` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
                             `rollback_info` longblob NOT NULL,
                             `log_status` int NOT NULL,
                             `log_created` datetime(0) NOT NULL,
                             `log_modified` datetime(0) NOT NULL,
                             `ext` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                             PRIMARY KEY (`id`) USING BTREE,
                             UNIQUE INDEX `ux_undo_log`(`xid`, `branch_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2838 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;
