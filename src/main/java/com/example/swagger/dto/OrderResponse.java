package com.example.swagger.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "订单信息")
public class OrderResponse {

    @Schema(description = "订单号", example = "OD20261120001")
    private final String orderNo;

    @Schema(description = "订单金额（元）", example = "299.00")
    private final BigDecimal amount;

    @Schema(description = "订单状态", example = "PAID")
    private final OrderStatus status;

    public OrderResponse(String orderNo, BigDecimal amount, OrderStatus status) {
        this.orderNo = orderNo;
        this.amount = amount;
        this.status = status;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
