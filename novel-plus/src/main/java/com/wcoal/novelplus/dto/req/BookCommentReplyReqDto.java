package com.wcoal.novelplus.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 小说评论回复请求DTO
 *
 * @author wcoal
 * @since 2025-11-12
 */
@Data
public class BookCommentReplyReqDto {

    @Schema(description = "评论ID", required = true)
    @NotNull(message = "评论ID不能为空")
    private Long commentId;

    @Schema(description = "回复内容", required = true)
    @NotBlank(message = "回复内容不能为空")
    private String replyContent;
}
