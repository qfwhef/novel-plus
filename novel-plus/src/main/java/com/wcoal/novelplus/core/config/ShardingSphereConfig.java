package com.wcoal.novelplus.core.config;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * ShardingSphere 分库分表配置
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "sharding", name = "enabled", havingValue = "true", matchIfMissing = false)
public class ShardingSphereConfig {

    @Bean(name = "dataSource")
    @Primary
    public DataSource shardingDataSource() throws SQLException, IOException {
        log.info("初始化 ShardingSphere 分片数据源...");
        
        ClassPathResource resource = new ClassPathResource("shardingsphere.yaml");
        
        // 使用 byte[] 读取配置文件，兼容 JAR 包部署
        byte[] yamlBytes = resource.getInputStream().readAllBytes();
        DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(yamlBytes);
        
        log.info("ShardingSphere 分片数据源初始化完成");
        return dataSource;
    }

    @Bean
    @Primary
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        log.info("初始化 MyBatis SqlSessionFactory...");
        
        MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        
        // 设置 MyBatis-Plus 配置
        com.baomidou.mybatisplus.core.MybatisConfiguration configuration = new com.baomidou.mybatisplus.core.MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        sessionFactory.setConfiguration(configuration);
        
        // 设置 Mapper XML 文件位置
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sessionFactory.setMapperLocations(resolver.getResources("classpath*:mapper/**/*.xml"));
        
        log.info("MyBatis SqlSessionFactory 初始化完成");
        return sessionFactory.getObject();
    }
}
