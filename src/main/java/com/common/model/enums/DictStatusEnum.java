package com.common.model.enums;

/**
 * ClassName: ResponseCodeEnum
 * Package: org.common.response
 * Description:
 *
 * @Author: @weixueshi
 * @Create: 2024/3/10 - 11:01
 * @Version: v1.0
 */

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 菜单状态：0：关闭；1：开启
 */
@Getter
public enum DictStatusEnum {
    CLOSE(0,"关闭"),
    OPEN(1,"开启");
    /**
     * 响应码
     */
    @EnumValue
    private final int code;

    /**
     * 响应消息
     */
    private final String status;

    /**
     * 如何定义一个通用的枚举类
     * 举值-->构造-->遍历
     */

    /**
     * 构造
     * @param code
     * @param status
     * @return
     */
    DictStatusEnum(int code, String status) {
        this.code = code;
        this.status = status;
    }

    /**
     * 遍历
     * @param code
     * @return
     */
    public static DictStatusEnum getResponseCodeEnum(int code) {
        for (DictStatusEnum menuTypeEnum : DictStatusEnum.values()) {
            if (menuTypeEnum.getCode() == code) {
                return menuTypeEnum;
            }
        }
        return null;
    }
}
