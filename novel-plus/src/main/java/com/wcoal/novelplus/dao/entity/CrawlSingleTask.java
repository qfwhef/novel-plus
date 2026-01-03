package com.wcoal.novelplus.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 单本采集任务实体
 *
 * @author wcoal
 * @since 2025-11-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("crawl_single_task")
@Schema(name = "CrawlSingleTask对象", description = "单本采集任务")
public class CrawlSingleTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "爬虫源ID")
    private Integer sourceId;

    @Schema(description = "源站小说ID")
    private String bookId;

    @Schema(description = "小说名")
    private String bookName;

    @Schema(description = "作者名")
    private String authorName;

    @Schema(description = "分类ID")
    private Integer catId;

    @Schema(description = "任务状态;0-失败 1-成功 2-排队中 3-采集中")
    private Byte taskStatus;

    @Schema(description = "执行次数")
    private Byte excCount;

    @Schema(description = "已采集章节数")
    private Integer crawlChapters;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
