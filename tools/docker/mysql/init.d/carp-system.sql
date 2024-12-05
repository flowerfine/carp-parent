create database if not exists carp default character set utf8mb4 collate utf8mb4_unicode_ci;
use carp;

/* 用户操作日志 */
drop table if exists carp_system_log_action;
create table carp_system_log_action
(
    id            bigint not null auto_increment comment '自增主键',
    `trace_id`    varchar(64) comment '链路ID',
    `module`      varchar(64) comment '模块',
    `desc`        varchar(255) comment '描述',
    `request`     varchar(255) comment 'http 请求',
    `response`    varchar(255) comment 'http 响应',
    `user`        varchar(64) comment 'http 用户信息',
    `start_time`  datetime comment 'http 请求开始时间',
    `end_time`    datetime comment 'http 请求结束时间',
    `duration`    int comment 'http 请求耗时',
    `status`      int comment 'http 请求状态',
    `creator`     varchar(32) comment '创建人',
    `create_time` timestamp default current_timestamp comment '创建时间',
    `editor`      varchar(32) comment '修改人',
    `update_time` timestamp default current_timestamp on update current_timestamp comment '修改时间',
    primary key (id),
    key           idx_module_status (`start_time`, `module`, `status`),
    key           idx_trace_id (`trace_id`)
) engine = innodb comment = 'system user action log';
