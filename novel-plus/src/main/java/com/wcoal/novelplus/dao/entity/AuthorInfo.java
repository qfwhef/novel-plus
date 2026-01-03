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
 * 作者信息
 * </p>
 *
 * @author wcoal
 * @since 2025-10-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("author_info")
@Schema(name="AuthorInfo对象", description="作者信息")
public class AuthorInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(name = "主键", description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(name = "用户ID", description = "用户ID")
    private Long userId;

    @Schema(name = "邀请码", description = "邀请码")
    private String inviteCode;

    @Schema(name = "笔名", description = "笔名")
    private String penName;

    @Schema(name = "手机号码", description = "手机号码")
    private String telPhone;

    @Schema(name = "QQ或微信账号", description = "QQ或微信账号")
    private String chatAccount;

    @Schema(name = "电子邮箱", description = "电子邮箱")
    private String email;

    @Schema(name = "作品方向", description = "作品方向;0-男频 1-女频")
    private Integer workDirection;

    @Schema(name = "状态", description = "0：正常;1-封禁")
    private Integer status;

    @Schema(name = "创建时间", description = "创建时间")
    private LocalDateTime createTime;

    @Schema(name = "更新时间", description = "更新时间")
    private LocalDateTime updateTime;


}
