package com.example.swagger.controller.order;

import com.example.swagger.dto.OrderResponse;
import com.example.swagger.dto.OrderStatus;
import com.example.swagger.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "订单管理", description = "订单查询接口，用于演示 GroupedOpenApi 文档分组与枚举 Schema")
public class OrderController {

    private static final Map<String, OrderResponse> ORDERS = new LinkedHashMap<>();

    static {
        ORDERS.put("OD20261120001",
                new OrderResponse("OD20261120001", new BigDecimal("299.00"), OrderStatus.PAID));
        ORDERS.put("OD20261120002",
                new OrderResponse("OD20261120002", new BigDecimal("89.50"), OrderStatus.CREATED));
        ORDERS.put("OD20261120003",
                new OrderResponse("OD20261120003", new BigDecimal("1280.00"), OrderStatus.SHIPPED));
    }

    @GetMapping
    @Operation(summary = "查询订单列表", description = "可按订单状态筛选，演示枚举参数")
    public List<OrderResponse> list(
            @Parameter(description = "订单状态（可选）", example = "PAID")
            @RequestParam(required = false) OrderStatus status) {
        return ORDERS.values().stream()
                .filter(order -> status == null || order.getStatus() == status)
                .toList();
    }

    @GetMapping("/{orderNo}")
    @Operation(summary = "根据订单号查询订单")
    public OrderResponse getByOrderNo(
            @Parameter(description = "订单号", example = "OD20261120001")
            @PathVariable String orderNo) {
        OrderResponse order = ORDERS.get(orderNo);
        if (order == null) {
            throw new ResourceNotFoundException("订单", orderNo);
        }
        return order;
    }
}
