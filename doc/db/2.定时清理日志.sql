delimiter $$
DROP event IF EXISTS xxl_job_log_clean;
create event xxl_job_log_clean
on schedule every 1 hour
do
begin
    start transaction;
    set @timenow=now(); #开始事务

		truncate table xxl_job_log;

    commit;  #提交事务
end  $$

#查看当前是否已开启事件调度器
#show variables like 'event_scheduler';
#要想保证能够执行event事件，就必须保证定时器是开启状态，默认为关闭状态
#set GLOBAL event_scheduler = ON;
#
# 停止
#ALTER EVENT xxl_job_log_clean DISABLE;
# 开启
#alter event xxl_job_log_clean enable;
# 查看状态
#select * from mysql.event

delimiter ;