package com.example.swagger.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * 全局 OpenAPI 文档信息（标题、描述、版本、联系人）。
 * 必须加 @Configuration，否则类上没有 Bean 定义时注解可能被编译器/AOT 优化掉。
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "用户管理系统 API",
                description = "用户管理模块接口文档",
                version = "1.0.0",
                contact = @Contact(
                        name = "开发团队",
                        email = "dev@example.com"
                )
        )
)
public class OpenApiConfig {
}
