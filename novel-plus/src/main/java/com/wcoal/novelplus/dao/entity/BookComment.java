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
 * 小说评论
 * </p>
 *
 * @author wcoal
 * @since 2025-10-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("book_comment")
@Schema(name="BookComment对象", description="小说评论")
public class BookComment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "评论小说ID")
    private Long bookId;

    @Schema(description = "评论用户ID")
    private Long userId;

    @Schema(description = "评价内容")
    private String commentContent;

    @Schema(description = "回复数量")
    private Integer replyCount;

    @Schema(description = "点赞数量")
    private Integer likeCount;

    @Schema(description = "审核状态;0-待审核 1-审核通过 2-审核不通过")
    private Integer auditStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;


}
