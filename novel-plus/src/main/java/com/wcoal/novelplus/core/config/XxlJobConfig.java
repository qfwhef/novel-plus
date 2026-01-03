package com.wcoal.novelplus.core.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * XXL-Job 配置类
 * 
 * 功能说明：
 * 1. 配置 XXL-Job 执行器
 * 2. 连接到 XXL-Job 调度中心
 * 3. 注册本地任务处理器
 * 
 * @author wcoal
 * @since 2025-10-10
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "xxl.job", name = "enabled", havingValue = "true")
public class XxlJobConfig {

    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;

    @Value("${xxl.job.executor.appname}")
    private String appname;

    @Value("${xxl.job.executor.logpath:logs/xxl-job/jobhandler}")
    private String logPath;

    @Value("${xxl.job.accessToken}")
    private String accessToken;

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info(">>>>>>>>>>> XXL-Job 执行器配置初始化.");
        
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(appname);
        xxlJobSpringExecutor.setLogPath(logPath);
        xxlJobSpringExecutor.setAccessToken(accessToken);

        log.info(">>>>>>>>>>> XXL-Job 执行器配置完成：");
        log.info("  - Admin地址: {}", adminAddresses);
        log.info("  - 应用名称: {}", appname);
        log.info("  - 日志路径: {}", logPath);
        
        return xxlJobSpringExecutor;
    }
}

