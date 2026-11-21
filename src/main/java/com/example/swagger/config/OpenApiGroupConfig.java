package com.example.swagger.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 按业务模块拆分文档分组，访问路径为 /v3/api-docs/{group}。
 * 分组名需唯一，扫描包路径须与工程实际结构一致。
 */
@Configuration
public class OpenApiGroupConfig {

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("用户模块")
                .packagesToScan("com.example.swagger.controller.user")
                .build();
    }

    @Bean
    public GroupedOpenApi orderApi() {
        return GroupedOpenApi.builder()
                .group("订单模块")
                .packagesToScan("com.example.swagger.controller.order")
                .build();
    }
}
