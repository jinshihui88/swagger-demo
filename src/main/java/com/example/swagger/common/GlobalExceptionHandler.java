package com.example.swagger.common;

import com.example.swagger.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 把参数校验、资源不存在、静态资源缺失等异常统一转为明确的 HTTP 状态码与错误体，
 * 避免落到 Spring Boot 默认兜底逻辑而产生 500 或不必要 ERROR 堆栈。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** @Valid 校验请求体失败：400 + 字段级错误明细 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                errors.merge(fieldError.getField(),
                        String.valueOf(fieldError.getDefaultMessage()),
                        (oldValue, newValue) -> oldValue + "；" + newValue));
        return new ApiError(400, "请求参数校验失败", errors);
    }

    /** 参数类型不匹配、必填参数缺失、请求体不可读、业务参数非法：400 */
    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            HttpMessageNotReadableException.class,
            ConstraintViolationException.class,
            IllegalArgumentException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(Exception ex) {
        return new ApiError(400, "请求参数不合法：" + ex.getMessage());
    }

    /** 资源不存在：404 */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(ResourceNotFoundException ex) {
        return new ApiError(404, ex.getMessage());
    }

    /** 静态资源不存在（如 /favicon.ico）：404，且不打印 ERROR 堆栈 */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNoResource(NoResourceFoundException ex) {
        return new ApiError(404, "资源不存在：" + ex.getResourcePath());
    }
}
