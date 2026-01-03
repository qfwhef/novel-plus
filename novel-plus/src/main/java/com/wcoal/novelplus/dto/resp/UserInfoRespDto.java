package com.wcoal.novelplus.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 用户信息 响应DTO
 *
 * @author wcoal
 * @since 2025-09-27
 */
@Data
@Builder
public class UserInfoRespDto {

    /**
     * 昵称
     * */
    @Schema(description = "昵称")
    private String nickName;

    /**
     * 用户头像
     * */
    @Schema(description = "用户头像")
    private String userPhoto;

    /**
     * 用户性别
     * */
    @Schema(description = "用户性别")
    private Integer gender;
}