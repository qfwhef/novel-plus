package com.wcoal.novelplus.dto.resp;

import com.wcoal.novelplus.core.common.enums.UserStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "用户登录响应DTO")
public class UserLoginRespDto {

    @Schema(description = "用户ID")
    private Long uid;

    @Schema(description = "用户token")
    private String token;

    @Schema(description = "用户名")
    private String userName;



    @Schema(description = "用户头像")
    private String userPhoto;

    @Schema(description = "昵称")
    private String nickName;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "性别：0-未知，1-男，2-女")
    private Integer gender;

    @Schema(description = "用户类型：1-普通用户，2-管理员，3-超级管理员")
    private Integer userType;

    @Schema(description = "用户状态：0-禁用，1-正常")
    private UserStatusEnum status;

    @Schema(description = "token过期时间（时间戳）")
    private Long expireTime;
}