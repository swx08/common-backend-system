package com.common.exception;

/**
 * ClassName: SystemException
 * Package: org.common.exception
 * Description:
 *
 * @Author: @weixueshi
 * @Create: 2024/3/10 - 13:28
 * @Version: v1.0
 */

import lombok.Data;
import com.common.response.ResponseCodeEnum;

/**
 * 统一系统异常
 */
@Data
public class SystemException extends Exception {

    /**
     * 异常码
     */
    private int code;

    /**
     * 异常消息
     */
    private String message;

    public SystemException(ResponseCodeEnum responseCodeEnum) {
        this.code = responseCodeEnum.getCode();
        this.message = responseCodeEnum.getMessage();
    }
}
