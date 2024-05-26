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
 * 用户状态：0：禁用；1：启用
 */
@Getter
public enum UserStatusEnum {
    CLOSE(0,"禁用"),
    OPEN(1,"启用");
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
     * @param status
     * @return
     */
    UserStatusEnum(int code, String status) {
        this.code = code;
        this.status = status;
    }

    /**
     * 遍历
     * @param code
     * @return
     */
    public static UserStatusEnum getResponseCodeEnum(int code) {
        for (UserStatusEnum menuTypeEnum : UserStatusEnum.values()) {
            if (menuTypeEnum.getCode() == code) {
                return menuTypeEnum;
            }
        }
        return null;
    }
}
