package com.lfz.carrental.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lfz.carrental.common.ApiResponse;
import com.lfz.carrental.common.SecurityUtils;
import com.lfz.carrental.dto.order.OrderCreateRequest;
import com.lfz.carrental.entity.RentalOrder;
import com.lfz.carrental.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<RentalOrder> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(orderService.createOrder(userId, request));
    }

    @GetMapping("/my")
    public ApiResponse<IPage<RentalOrder>> myOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(orderService.myOrders(userId, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<RentalOrder> getOrder(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(orderService.getOrderByIdForUser(userId, id));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<Void> cancelOrder(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        orderService.cancelOrder(userId, id);
        return ApiResponse.success("订单已取消");
    }

    @PostMapping("/{id}/pay")
    public ApiResponse<Void> payOrder(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        orderService.payOrder(userId, id);
        return ApiResponse.success("支付成功，待取车");
    }
}

