# 数据库初始化

-- 创建库
create database if not exists aigc;

-- 切换库
use aigc;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_userAccount (userAccount)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 数字人信息表
create table if not exists aiPersonInfo
(
    id           bigint auto_increment comment 'id' primary key,
    userId       bigint                                 not null comment '创建此数字人的用户的Id',
    aiName       varchar(256)                           not null comment '给AI设定的称谓',
    aiProfile    text                                   not null comment '对模拟对象的简单描述',
    aiVoice      varchar(1024)                          not null comment '模拟对象的音频文件地址',
    aiPicture    varchar(1024)                              null comment '模拟对象的图片（外貌）文件地址',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    foreign key (userId) references user(id),
    index idx_userId (userId)
) comment '数字人信息表' collate = utf8mb4_unicode_ci;

-- 数字人聊天表
create table if not exists message
(
    id           bigint auto_increment comment 'id' primary key,
    userId       bigint                                 not null comment '用户的Id',
    aiPersonId   bigint                                 not null comment '数字人的Id',
    content      text                                   not null comment '聊天记录',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    foreign key (userId)      references user(id),
    foreign key (aiPersonId)  references aiPersonInfo(id)
) comment '聊天记录表' collate = utf8mb4_unicode_ci;

-- 漂流瓶表
create table if not exists bottle
(
    id           bigint auto_increment comment 'id' primary key,
    userId       bigint                                 not null comment '发送此瓶子的用户的Id',
    pickUserId   bigint                                     null comment '捞起此瓶子的用户的Id',
    content      text                                   not null comment '漂流瓶内容',
    isPick       tinyint      default 0                 not null comment '是否被打捞',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    foreign key (userId)      references user(id)
) comment '漂流瓶表' collate = utf8mb4_unicode_ci;

create table if not exists bottleComment
(
    id           bigint auto_increment comment 'id' primary key,
    bottleId     bigint                                 not null comment '瓶子的Id',
    parentId     bigint       default 0                 not null comment '此条评论的父评论,0时表示为顶级评论',
    userId       bigint                                 not null comment '发布此条评论的id',
    replyUserId  bigint                                 not null comment '被回复的用户的id',
    detail       varchar(1024)                          not null comment '评论的内容',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    foreign key (bottleId)      references bottle(id)
) comment '漂流瓶评论表' collate = utf8mb4_unicode_ci;

-- 聊天记录表
create table if not exists message
(
    id           bigint auto_increment comment 'id' primary key,
    userId       bigint                                 not null comment '用户的Id',
    aiPersonId   bigint                                 not null comment '数字人的Id',
    content      text                                   not null comment '聊天记录',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    foreign key (userId)      references user(id),
    foreign key (aiPersonId)  references aiPersonInfo(id)
) comment '聊天记录表' collate = utf8mb4_unicode_ci;
