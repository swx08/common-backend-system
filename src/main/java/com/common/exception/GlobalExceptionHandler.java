package com.common.exception;

/**
 * ClassName: GlobalExceptionHandler
 * Package: org.common.exception
 * Description:
 *
 * @Author: @weixueshi
 * @Create: 2024/3/10 - 11:59
 * @Version: v1.0
 */

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import lombok.extern.slf4j.Slf4j;
import com.common.response.ResponseCodeEnum;
import com.common.response.ResultData;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResultData systemExceptionHandler(Exception e){
        log.info("运行时异常：",e);
        return ResultData.fail(ResponseCodeEnum.NEED_LOGIN);
    }

    @ExceptionHandler(SystemException.class)
    public ResultData systemExceptionHandler(SystemException e){
        log.info("自定义系统异常：",e);
        return ResultData.fail(e.getCode(),e.getMessage());
    }

    /**
     * 参数未传异常
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultData validExceptionHandler(HttpServletRequest request, MethodArgumentNotValidException e){
        log.info("参数未传异常：",e);
        printLog(request,e);
        Map<String,String> map = new HashMap<String,String>();
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        fieldErrors.forEach(fieldError -> {
            map.put(fieldError.getField(),fieldError.getDefaultMessage());
        });
        return ResultData.fail(ResponseCodeEnum.PARAMETER_ERROR,map);
    }

    /**
     * 捕获SQL异常
     */
    @ExceptionHandler(SQLException.class)
    public ResultData sqlException(HttpServletRequest request, SQLException e){
        printLog(request,e);
        log.error("报SQL异常：{}",e.getMessage());
        return ResultData.fail(ResponseCodeEnum.FAIL.getCode(),e.getMessage());
    }

    /**
     * sa-token：token无效异常
     */
    @ExceptionHandler(NotLoginException.class)
    public ResultData notLoginException(HttpServletRequest request, NotLoginException e){
        printLog(request,e);
        log.error("报token无效异常：{}",e.getMessage());
        return ResultData.fail(ResponseCodeEnum.LOGIN_EXPIRE);
    }

    /**
     * sa-token：无此权限异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public ResultData notPermissionException(HttpServletRequest request, NotPermissionException e){
        printLog(request,e);
        log.error("无此权限异常：{}",e.getMessage());
        return ResultData.fail(ResponseCodeEnum.NO_PERMISSION);
    }

    /**
     * sa-token：无此角色异常
     */
    @ExceptionHandler(NotRoleException.class)
    public ResultData notRoleException(HttpServletRequest request, NotRoleException e){
        printLog(request,e);
        log.error("无此角色异常：{}",e.getMessage());
        return ResultData.fail(ResponseCodeEnum.NO_PERMISSION);
    }

    /**
     * 打印日志信息
     * @param request
     * @param exception
     */
    private static void printLog(HttpServletRequest request, Exception exception) {
        //换行符
        String lineSeparatorStr = System.getProperty("line.separator");

        StringBuilder exStr = new StringBuilder();
        StackTraceElement[] trace = exception.getStackTrace();
        // 获取堆栈信息并输出为打印的形式
        for (StackTraceElement s : trace) {
            exStr.append("\tat " + s + "\r\n");
        }
        //打印error级别的堆栈日志
        log.error("访问地址：" + request.getRequestURL() + ",请求方法：" + request.getMethod() +
                ",远程地址：" + request.getRemoteAddr() + lineSeparatorStr +
                "错误堆栈信息如下:" + exception.toString() + lineSeparatorStr + exStr);
    }
}
