package com.wcoal.novelplus.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "用户注册响应DTO")
public class UserRegisterReqDto {

    @Schema(description = "用户名")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String userName;

    @Schema(description = "密码")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    @Schema(description = "确认密码")
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @Schema(description = "邮箱")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "昵称")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickName;

    @Schema(description = "性别：0-未知，1-男，2-女")
    private Integer gender = 0;

    @Schema(description = "验证码", required = true)
    @NotBlank(message="验证码不能为空！")
    @Pattern(regexp="^\\d{4}$",message="验证码格式不正确！")
    private String velCode;

    /**
     * 请求会话标识，用来标识图形验证码属于哪个会话
     * */
    @Schema(description = "sessionId", required = true)
    @NotBlank
    private String sessionId;

}
