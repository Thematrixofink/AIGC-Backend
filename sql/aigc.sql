/*
 Navicat Premium Data Transfer

 Source Server         : 本机MySql
 Source Server Type    : MySQL
 Source Server Version : 50740
 Source Host           : localhost:3306
 Source Schema         : aigc

 Target Server Type    : MySQL
 Target Server Version : 50740
 File Encoding         : 65001

 Date: 24/03/2024 15:28:00
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for aipersoninfo
-- ----------------------------
DROP TABLE IF EXISTS `aipersoninfo`;
CREATE TABLE `aipersoninfo`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `userId` bigint(20) NOT NULL COMMENT '创建此数字人的用户的Id',
  `aiName` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '给AI设定的称谓',
  `aiProfile` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '对模拟对象的简单描述',
  `aiVoice` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模拟对象的音频文件地址',
  `aiPicture` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模拟对象的图片（外貌）文件地址',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否删除',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态：0-待生成  1-生成中  2-生成成功  3-生成失败',
  `execMessage` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '执行信息',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_userId`(`userId`) USING BTREE,
  CONSTRAINT `aipersoninfo_ibfk_1` FOREIGN KEY (`userId`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1767894075244584962 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数字人信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bottle
-- ----------------------------
DROP TABLE IF EXISTS `bottle`;
CREATE TABLE `bottle`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `userId` bigint(20) NOT NULL COMMENT '发送此瓶子的用户的Id',
  `pickUserId` bigint(20) NULL DEFAULT NULL COMMENT '捞起此瓶子的用户的Id',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '漂流瓶内容',
  `isPick` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否被打捞',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `userId`(`userId`) USING BTREE,
  CONSTRAINT `bottle_ibfk_1` FOREIGN KEY (`userId`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1766392851538149379 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '漂流瓶表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bottlecomment
-- ----------------------------
DROP TABLE IF EXISTS `bottlecomment`;
CREATE TABLE `bottlecomment`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `bottleId` bigint(20) NOT NULL COMMENT '瓶子的Id',
  `parentId` bigint(20) NOT NULL DEFAULT 0 COMMENT '此条评论的父评论,0时表示为顶级评论',
  `userId` bigint(20) NOT NULL COMMENT '发布此条评论的id',
  `replyUserId` bigint(20) NOT NULL COMMENT '被回复的用户的id',
  `detail` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论的内容',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `bottleId`(`bottleId`) USING BTREE,
  CONSTRAINT `bottlecomment_ibfk_1` FOREIGN KEY (`bottleId`) REFERENCES `bottle` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '漂流瓶评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `userId` bigint(20) NOT NULL COMMENT '用户的Id',
  `aiPersonId` bigint(20) NOT NULL COMMENT '数字人的Id',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '聊天记录',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `userId`(`userId`) USING BTREE,
  INDEX `aiPersonId`(`aiPersonId`) USING BTREE,
  CONSTRAINT `message_ibfk_1` FOREIGN KEY (`userId`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `message_ibfk_2` FOREIGN KEY (`aiPersonId`) REFERENCES `aipersoninfo` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1769668407067160579 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '聊天记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `userAccount` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号',
  `userPassword` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `userName` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `userAvatar` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户头像',
  `userProfile` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户简介',
  `userRole` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'user' COMMENT '用户角色：user/admin',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否删除',
  `aigcCount` int(11) NOT NULL COMMENT 'aigc功能剩余的使用次数',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_userAccount`(`userAccount`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1765693622058573826 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
