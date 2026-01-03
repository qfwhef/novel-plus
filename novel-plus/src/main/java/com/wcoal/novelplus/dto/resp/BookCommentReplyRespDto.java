package com.wcoal.novelplus.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说评论回复响应DTO
 *
 * @author wcoal
 * @since 2025-11-12
 */
@Data
@Builder
public class BookCommentReplyRespDto {

    @Schema(description = "回复ID")
    private Long id;

    @Schema(description = "评论ID")
    private Long commentId;

    @Schema(description = "回复用户ID")
    private Long userId;

    @Schema(description = "回复用户名")
    private String username;

    @Schema(description = "回复用户头像")
    private String userPhoto;

    @Schema(description = "回复内容")
    private String replyContent;

    @Schema(description = "回复时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
