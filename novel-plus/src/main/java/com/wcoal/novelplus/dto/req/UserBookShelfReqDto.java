package com.wcoal.novelplus.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户书架请求参数
 */
@Data
public class UserBookShelfReqDto {

    /**
     * 小说ID
     */
    @Schema(description = "小说ID")
    private Long bookId;
}
