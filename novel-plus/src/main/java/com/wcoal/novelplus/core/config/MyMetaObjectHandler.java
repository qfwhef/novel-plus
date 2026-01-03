package com.wcoal.novelplus.core.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 * 用于自动填充 create_time 和 update_time 字段
 *
 * @author wcoal
 * @since 2025-11-12
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("开始插入填充...");
        
        // 填充 createTime
        if (metaObject.hasGetter("createTime")) {
            Object createTime = this.getFieldValByName("createTime", metaObject);
            if (createTime == null) {
                this.setFieldValByName("createTime", LocalDateTime.now(), metaObject);
                log.info("自动填充 createTime: {}", LocalDateTime.now());
            }
        }
        
        // 填充 updateTime
        if (metaObject.hasGetter("updateTime")) {
            Object updateTime = this.getFieldValByName("updateTime", metaObject);
            if (updateTime == null) {
                this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
                log.info("自动填充 updateTime: {}", LocalDateTime.now());
            }
        }
    }

    /**
     * 更新时自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("开始更新填充...");
        
        // 填充 updateTime
        if (metaObject.hasGetter("updateTime")) {
            this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
            log.info("自动填充 updateTime: {}", LocalDateTime.now());
        }
    }
}
