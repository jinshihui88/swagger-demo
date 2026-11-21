package com.example.swagger.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户信息")
public class UserResponse {

    @Schema(description = "用户ID", example = "1001")
    private final Long id;

    @Schema(description = "用户姓名", example = "张三")
    private final String username;

    @Schema(description = "电子邮箱", example = "zhangsan@example.com")
    private final String email;

    public UserResponse() {
        this(null, null, null);
    }

    public UserResponse(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
