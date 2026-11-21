package com.example.swagger.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 真实枚举：springdoc 会自动把枚举值写入 OpenAPI Schema 的 enum 列表。
 */
@Schema(description = "订单状态")
public enum OrderStatus {

    CREATED("已创建"),
    PAID("已支付"),
    SHIPPED("已发货"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
