package com.wcoal.novelplus.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 新闻内容
 * </p>
 *
 * @author wcoal
 * @since 2025-10-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("news_content")
@Schema(name="NewsContent对象", description="新闻内容")
public class NewsContent implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(name="主键", description="主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(name="新闻ID", description="新闻ID")
    private Long newsId;

    @Schema(name="新闻内容", description="新闻内容")
    private String content;

    @Schema(name="创建时间", description="创建时间")
    private LocalDateTime createTime;

    @Schema(name="更新时间", description="更新时间")
    private LocalDateTime updateTime;


}
