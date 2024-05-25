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
 * 菜单类型：0：目录；1：菜单；2：按钮
 */
@Getter
public enum MenuTypeEnum {
    DIRECTORY(0,"目录"),
    MENU(1,"菜单"),
    BUTTON(2,"按钮");
    /**
     * 响应码
     */
    private final int code;

    /**
     * 响应消息
     */
    private final String type;

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
    MenuTypeEnum(int code, String type) {
        this.code = code;
        this.type = type;
    }

    /**
     * 遍历
     * @param code
     * @return
     */
    public static MenuTypeEnum getResponseCodeEnum(int code) {
        for (MenuTypeEnum menuTypeEnum : MenuTypeEnum.values()) {
            if (menuTypeEnum.getCode() == code) {
                return menuTypeEnum;
            }
        }
        return null;
    }
}
