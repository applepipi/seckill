--创建数据库
CREATE database seckill;
--使用当前数据库
use seckill;
--创建秒杀商品表
CREATE TABLE seckill(
`seckill_id` bigint NOT NULL auto_increment comment '商品库存id',
`name` VARCHAR(120) NOT NULL comment '商品名称',
`number` int not null comment '库存数量',
`start_time` TIMESTAMP not NULL comment '秒杀开启时间',
`end_time` TIMESTAMP not NULL comment '秒杀结束时间',
`create_time` TIMESTAMP not NULL DEFAULT CURRENT_TIMESTAMP comment '创建时间',
PRIMARY KEY (seckill_id),
KEY idx_start_time(start_time),
KEY idx_end_time(end_time),
KEY idx_create_time(create_time)
)ENGINE=InnoDB auto_increment=1000 DEFAULT charset=utf8 comment='秒杀库存表';
--初始化数据
INSERT INTO
  seckill(name,number,start_time,end_time)
VALUES
  ('1000元秒杀iphone6',100,'2015-11-01 00:00:00','2015-11-02 00:00:00'),
  ('100元秒杀ipad',200,'2015-11-01 00:00:00','2015-11-02 00:00:00'),
  ('300元秒杀小米4',300,'2015-11-01 00:00:00','2015-11-02 00:00:00'),
  ('200元秒杀iphone6',400,'2015-11-01 00:00:00','2015-11-02 00:00:00');
--秒杀成功明细表
--用户登录认证相关的信息
create TABLE success_killed(
  `seckill_id` bigint not null comment '秒杀商品id',
  `user_phone` bigint not NULL comment '用户手机号',
  `state` tinyint not NULL DEFAULT -1 comment '状态标识-1无效0成功1已付款',
  `create_time` TIMESTAMP not NULL comment '创建时间',
  PRIMARY KEY (seckill_id,user_phone),
  KEY idx_create_time(create_time)
)engine=innodb DEFAULT charset=utf8 comment='秒杀成功明细表';