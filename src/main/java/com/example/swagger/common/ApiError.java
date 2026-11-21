package com.example.swagger.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * 统一错误响应体。成功响应为业务 DTO 本身，异常响应统一使用该结构。
 */
@Schema(description = "统一错误响应")
public class ApiError {

    @Schema(description = "HTTP 状态码", example = "404")
    private final int code;

    @Schema(description = "错误描述", example = "用户不存在")
    private final String message;

    @Schema(description = "字段级校验错误：key 为字段名，value 为提示")
    private final Map<String, String> fieldErrors;

    @Schema(description = "错误发生时间戳（epoch 毫秒）", example = "1763635200000")
    private final long timestamp;

    public ApiError(int code, String message) {
        this(code, message, Map.of());
    }

    public ApiError(int code, String message, Map<String, String> fieldErrors) {
        this.code = code;
        this.message = message;
        this.fieldErrors = fieldErrors == null ? Map.of() : fieldErrors;
        this.timestamp = System.currentTimeMillis();
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
