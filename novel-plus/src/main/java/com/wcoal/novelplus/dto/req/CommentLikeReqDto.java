package com.wcoal.novelplus.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评论点赞请求 DTO
 *
 * @author wcoal
 * @since 2025-11-13
 */
@Data
@Schema(description = "评论点赞请求")
public class CommentLikeReqDto {

    @Schema(description = "评论ID", required = true)
    @NotNull(message = "评论ID不能为空")
    private Long commentId;

    @Schema(description = "是否点赞（true-点赞，false-取消点赞）", required = true)
    @NotNull(message = "点赞状态不能为空")
    private Boolean liked;
}
