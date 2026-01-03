package com.wcoal.novelplus.core.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.wcoal.novelplus.core.validate.annotations.EnumValid;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举类。
 * <p>
 * 错误码为字符串类型，共 5 位，分成两个部分：错误产生来源+四位数字编号。 错误产生来源分为 A/B/C， A 表示错误来源于用户，比如参数错误，用户安装版本过低，用户支付 超时等问题； B
 * 表示错误来源于当前系统，往往是业务逻辑出错，或程序健壮性差等问题； C 表示错误来源 于第三方服务，比如 CDN 服务出错，消息投递超时等问题；四位数字编号从 0001 到 9999，大类之间的
 * 步长间距预留 100。
 * <p>
 * 错误码分为一级宏观错误码、二级宏观错误码、三级宏观错误码。 在无法更加具体确定的错误场景中，可以直接使用一级宏观错误码。
 *
 * @author xiongxiaoyang
 * @since 2025/9/25
 */
@Getter
@AllArgsConstructor
public enum UserStatusEnum implements BaseEnum{

    NORMAL(0, "正常"),
    DISABLE(1, "禁用");

    /**
     * 错误码
     */
    @EnumValue
    private final Integer code;

    /**
     * 中文描述
     */
    private final String message;

    @Override
    public int getValue() {
        return code;
    }

    @Override
    public String getDesc() {
        return message;
    }
}
