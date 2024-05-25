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

import lombok.Getter;

/**
 * 菜单状态：0：关闭；1：开启
 */
@Getter
public enum MenuStatusEnum {
    CLOSE(0,"关闭"),
    OPEN(1,"开启");
    /**
     * 响应码
     */
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
     * @param type
     * @return
     */
    MenuStatusEnum(int code, String status) {
        this.code = code;
        this.status = status;
    }

    /**
     * 遍历
     * @param code
     * @return
     */
    public static MenuStatusEnum getResponseCodeEnum(int code) {
        for (MenuStatusEnum menuTypeEnum : MenuStatusEnum.values()) {
            if (menuTypeEnum.getCode() == code) {
                return menuTypeEnum;
            }
        }
        return null;
    }
}
