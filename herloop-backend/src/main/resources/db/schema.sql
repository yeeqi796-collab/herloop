-- HerLoop 数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS herloop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE herloop;

-- ============================================
-- 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱(登录账号)',
    `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt)',
    `avatar` VARCHAR(100) DEFAULT '🦢' COMMENT '头像(emoji或URL)',
    `wechat` VARCHAR(100) DEFAULT NULL COMMENT '微信号',
    `points` INT NOT NULL DEFAULT 0 COMMENT '积分余额',
    `verified` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否认证 0-否 1-是',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 商品表
-- ============================================
CREATE TABLE IF NOT EXISTS `product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '发布者ID',
    `title` VARCHAR(100) NOT NULL COMMENT '商品标题',
    `category` VARCHAR(20) NOT NULL COMMENT '分类: 服饰/图书/美妆/运动/生活',
    `condition_desc` VARCHAR(50) NOT NULL COMMENT '成色描述',
    `description` TEXT COMMENT '详细描述',
    `cash_price` DECIMAL(10,2) DEFAULT 0 COMMENT '现金价格',
    `points_price` INT DEFAULT 0 COMMENT '积分价格',
    `trade_mode` VARCHAR(10) NOT NULL COMMENT '交易方式: cash/points/both',
    `status` VARCHAR(10) NOT NULL DEFAULT 'on' COMMENT '状态: on/reserved/sold',
    `icon` VARCHAR(30) DEFAULT 'bag' COMMENT '商品图标类型',
    `wechat` VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ============================================
-- 求购表
-- ============================================
CREATE TABLE IF NOT EXISTS `want` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '发布者ID',
    `title` VARCHAR(100) NOT NULL COMMENT '求购标题',
    `budget` VARCHAR(50) NOT NULL COMMENT '预算描述',
    `description` TEXT COMMENT '求购说明',
    `icon` VARCHAR(30) DEFAULT 'bag' COMMENT '图标类型',
    `status` VARCHAR(10) NOT NULL DEFAULT 'open' COMMENT '状态: open/closed',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='求购表';

-- ============================================
-- 交易记录表
-- ============================================
CREATE TABLE IF NOT EXISTS `trade` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `buyer_id` BIGINT NOT NULL COMMENT '买家ID',
    `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
    `type` VARCHAR(10) NOT NULL COMMENT '对当前用户的类型: buy/sell',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '交易状态: pending/completed/cancelled',
    `trade_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '交易时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_buyer` (`buyer_id`),
    KEY `idx_seller` (`seller_id`),
    KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易记录表';

-- ============================================
-- 积分流水表
-- ============================================
CREATE TABLE IF NOT EXISTS `points_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `amount` INT NOT NULL COMMENT '变动数量(正为收入，负为支出)',
    `description` VARCHAR(200) NOT NULL COMMENT '变动说明',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水表';

-- ============================================
-- 收藏表
-- ============================================
CREATE TABLE IF NOT EXISTS `favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';
