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
 * 小说内容
 * </p>
 *
 * @author wcoal
 * @since 2025-10-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("book_content")
@Schema(name="BookContent对象", description="小说内容")
public class BookContent implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "章节ID")
    private Long chapterId;

    @Schema(description = "小说章节内容")
    private String content;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
