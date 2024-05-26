package com.common.config.interceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: LoginInterceptorConfig
 * Package: com.sixkey.interceptorconfig
 * Description:
 *
 * @Author: @weixueshi
 * @Create: 2023/11/1 - 21:56
 * @Version: v1.0
 */
@Configuration
public class LoginInterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //所谓白名单就是用户没有登录就可以访问的路径
        //黑名单就是用户没有登录就不可以访问的路径
        //1.创建自定义的拦截器对象
        //2.配置白名单并存放在一个List集合
        List<String> patters = new ArrayList<>();
        patters.add("/user/login");
        patters.add("/user/register");


        //registry.addInterceptor(interceptor);完成拦截
        // 器的注册,后面的addPathPatterns表示拦截哪些url
        //这里的参数/**表示所有请求,再后面的excludePathPatterns表
        // 示有哪些是白名单,且参数是列表
        registry.addInterceptor(loginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(patters);
    }

    /**
     * 这是自定义的拦截器交给spring管理
     * @return
     */
    @Bean
    public LoginInterceptor loginInterceptor() {
        return new LoginInterceptor();
    }
}
