package com.example.swagger.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * 声明 Bearer JWT 认证方案：Swagger UI 右上角出现 Authorize 按钮。
 * 注意：这里只描述文档层的认证方式，不替代后端 Spring Security 鉴权。
 */
@Configuration
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "请输入JWT令牌，不需要手动添加Bearer前缀"
)
public class SwaggerSecurityConfig {
}
