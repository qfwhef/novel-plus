package com.wcoal.novelplus.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

// 创建 BookVisitReqDto.java
@Data
public class BookVisitReqDto {
    @Schema(description = "小说ID")
    private Long bookId;
}
