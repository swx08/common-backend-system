package com.common.response;

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
 * 统一响应枚举值
 */
@Getter
public enum ResponseCodeEnum {
    FAIL(403,"请求失败"),
    SUCCESS(200,"请求成功"),
    NEED_LOGIN(402,"需要登录后操作"),
    LOGIN_EXPIRE(405,"登录过期，请重新登录！"),
    SYSTEM_ERROR(500,"服务器开小差，稍后重试"),
    NO_PERMISSION(406,"无此权限！"),

    //业务码
    ADMIN_NORMAL(1001,"状态正常，无法删除！"),
    USER_NOT_EXITS(1002,"用户不存在！"),
    PHONE_ERROR(1003,"手机号错误！"),
    PASSWOR_ERROR(1004,"密码错误！"),
    PERMISSION_NAME_ILLEGAL(1005,"权限标识命名不规范！"),
    COMPONENT_NAME_ILLEGAL(1006,"组件路径命名不规范！"),
    MENU_NAME_EXITS(1007,"组件名称已存在！"),
    MENU_TITLE_EXITS(1008,"菜单名称已存在！"),
    INCLUDE_SUBMENU(1009,"包含子菜单！无法删除"),
    ROLE_NAME_EXITS(1010,"角色名称已存在"),
    ROLE_CODE_EXITS(1011,"角色标识已存在"),
    INSUFFICIENT_AUTHORITY(1012,"无权操作管理员角色！"),
    USERNAME_EXITS(1014,"用户名称已存在！"),
    PHONE_EXITS(1015,"手机号已存在！"),
    EMAIL_EXITS(1016,"邮箱已存在！"),
    USER_FORBIDDEN(1018,"用户已被禁用，请联系管理员恢复！"),
    ROLE_NOT_EXITS(1025,"角色不存在！");

    /**
     * 响应码
     */
    private final int code;

    /**
     * 响应消息
     */
    private final String message;

    /**
     * 如何定义一个通用的枚举类
     * 举值-->构造-->遍历
     */

    /**
     * 构造
     * @param code
     * @param message
     * @return
     */
    ResponseCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 遍历
     * @param code
     * @return
     */
    public static  ResponseCodeEnum getResponseCodeEnum(int code) {
        for (ResponseCodeEnum responseCodeEnum : ResponseCodeEnum.values()) {
            if (responseCodeEnum.getCode() == code) {
                return responseCodeEnum;
            }
        }
        return null;
    }
}
