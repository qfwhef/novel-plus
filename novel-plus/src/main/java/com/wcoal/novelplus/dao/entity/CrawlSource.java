package com.wcoal.novelplus.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 爬虫源实体
 *
 * @author wcoal
 * @since 2025-11-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("crawl_source")
@Schema(name = "CrawlSource对象", description = "爬虫源")
public class CrawlSource implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "爬虫源名称")
    private String sourceName;

    @Schema(description = "爬虫规则JSON")
    private String crawlRule;

    @Schema(description = "爬虫源状态;0-关闭 1-开启")
    private Byte sourceStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
