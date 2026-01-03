package com.wcoal.novelplus.dao.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;
import com.wcoal.novelplus.core.common.enums.UserStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户信息表
 * </p>
 *
 * @author wcoal
 * @since 2025-09-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_info")
@Schema(description="用户信息表")
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID，主键自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户名，唯一标识")
    private String userName;

    @Schema(description = "密码，加密存储")
    private String password;

    @Schema(description = "邮箱地址")
    private String email;

    @Schema(description = "昵称")
    private String nickName;

    @Schema(description = "用户头像")
    private String userPhoto;

    @Schema(description = "性别：0-未知，1-男，2-女")
    private Integer gender = 0;

    @Schema(description = "用户类型：1-普通用户，2-管理员，3-超级管理员")
    private Integer userType = 1;

    @Schema(description = "用户状态：0-禁用，1-正常")
    @TableField("status")
    private UserStatusEnum status = UserStatusEnum.NORMAL;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "是否删除：0-未删除，1-已删除")
    private Integer isDeleted = 0;

    @Schema(description = "注册时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


}
