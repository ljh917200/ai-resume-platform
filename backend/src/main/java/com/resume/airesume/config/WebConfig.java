package com.resume.airesume.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
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

    // 从配置文件读取上传路径
    @Value("${file.upload.path}")
    private String uploadPath;

    // 从配置文件读取URL前缀
    @Value("${file.upload.url-prefix}")
    private String urlPrefix;

    /**
     * 配置静态资源映射
     * 将 /uploads/** 的请求映射到实际的文件存储目录
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 URL 路径 /uploads/** 映射到本地文件目录
        // 例如：访问 /uploads/avatars/user_1_xxx.jpg 会映射到 uploadPath/avatars/user_1_xxx.jpg
        registry.addResourceHandler(urlPrefix + "/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}