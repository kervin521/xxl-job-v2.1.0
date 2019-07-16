/*
 Navicat Premium Data Transfer

 Source Server         : 172.21.32.102[3307]
 Source Server Type    : MySQL
 Source Server Version : 50722
 Source Host           : 172.21.32.102:3307
 Source Schema         : xxl-job

 Target Server Type    : MySQL
 Target Server Version : 50722
 File Encoding         : 65001

 Date: 16/07/2019 15:51:09
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for xxl_job_group
-- ----------------------------
DROP TABLE IF EXISTS `xxl_job_group`;
CREATE TABLE `xxl_job_group`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `app_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '执行器AppName',
  `title` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '执行器名称',
  `order` tinyint(4) NOT NULL DEFAULT 0 COMMENT '排序',
  `address_type` tinyint(4) NOT NULL DEFAULT 0 COMMENT '执行器地址类型：0=自动注册、1=手动录入',
  `address_list` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '执行器地址列表，多地址逗号分隔',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of xxl_job_group
-- ----------------------------
INSERT INTO `xxl_job_group` VALUES (1, 'xxl-job-executor-sample', '示例执行器', 1, 0, NULL);
INSERT INTO `xxl_job_group` VALUES (2, 'target-calculate-service', 'calculate-dev', 1, 0, NULL);
INSERT INTO `xxl_job_group` VALUES (3, 'target-calculate-service-pro', 'calculate-pro', 1, 0, '172.19.0.21:65534');
INSERT INTO `xxl_job_group` VALUES (4, 'target-calculate-service-lua', 'calculate-lua', 1, 0, NULL);
INSERT INTO `xxl_job_group` VALUES (5, 'target-calculate-service-test', 'calculate-test', 1, 0, NULL);
INSERT INTO `xxl_job_group` VALUES (6, 'runLua', 'runLua', 1, 0, NULL);

SET FOREIGN_KEY_CHECKS = 1;
