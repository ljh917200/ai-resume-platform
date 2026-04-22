package com.resume.airesume.config;

import com.resume.airesume.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 拦截器
 * 作用：拦截需要登录的请求，验证 Token 是否有效
 */
@Component  // 标记为Spring组件，自动注入到容器中
public class JwtInterceptor implements HandlerInterceptor {

    /**
     * 在请求到达 Controller 之前执行
     *
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理器（Controller方法）
     * @return true=放行，false=拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. 从请求头获取 Token
        // 前端需要在 Header 中添加：Authorization: Bearer <token>
        String authHeader = request.getHeader("Authorization");

        // 2. 检查 Token 是否存在
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Token 不存在或格式错误，返回401未授权
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录，请先登录\"}");
            return false;  // 拦截请求，不放行
        }

        // 3. 提取 Token（去掉 "Bearer " 前缀）
        String token = authHeader.replace("Bearer ", "");

        // 4. 验证并解析 Token
        try {
            // 调用工具类解析 Token
            Claims claims = JwtUtil.parseToken(token);

            // 5. 从 Token 中提取用户信息
            Long userId = claims.get("userId", Long.class);
            String username = claims.get("username", String.class);
            String email = claims.get("email", String.class);

            // 6. 将用户信息存入 request，供 Controller 使用
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("email", email);

            // 7. 验证成功，放行请求
            return true;

        } catch (Exception e) {
            // Token 无效或已过期
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期，请重新登录\"}");
            return false;  // 拦截请求
        }
    }
}