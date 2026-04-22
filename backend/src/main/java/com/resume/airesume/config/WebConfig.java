package com.resume.airesume.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类
 * 作用：注册拦截器，配置哪些接口需要登录验证
 */
@Configuration  // 标记为配置类
public class WebConfig implements WebMvcConfigurer {

    @Autowired  // 自动注入 JWT 拦截器
    private JwtInterceptor jwtInterceptor;

    /**
     * 注册拦截器
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)          // 添加 JWT 拦截器
                .addPathPatterns("/api/**")              // 拦截所有 /api/ 开头的请求
                .excludePathPatterns(                    // 排除不需要登录的接口
                        "/api/auth/register",            // 注册接口不需要登录
                        "/api/auth/login/**"             // 登录接口不需要登录
                );
    }
}