package com.common.config.interceptor;

/**
 * ClassName: LoginInterceptor
 * Package: com.sixkey.interceptorconfig
 * Description:
 *
 * @Author: @weixueshi
 * @Create: 2023/11/1 - 21:50
 * @Version: v1.0
 */

import cn.dev33.satoken.stp.StpUtil;
import com.common.exception.SystemException;
import com.common.response.ResponseCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义配置请求拦截器，所有的请求都会先通过这里
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * 请求转发之前执行
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从请求头中获取token
        String token = request.getHeader("authorization");
        //过滤接口文档token
        if(StringUtils.equalsIgnoreCase("Basic YWRtaW46YWRtaW4=", token)){
            return true;
        }
        if(StringUtils.isEmpty(token)){
            throw new SystemException(ResponseCodeEnum.NEED_LOGIN);
        }
        // 先检查是否已被冻结
        StpUtil.checkActiveTimeout();
        // 为指定 Token 续签
        StpUtil.stpLogic.updateLastActiveToNow(token);
        return true;
    }
}
