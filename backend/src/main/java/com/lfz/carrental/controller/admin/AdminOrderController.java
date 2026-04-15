package com.lfz.carrental.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lfz.carrental.common.ApiResponse;
import com.lfz.carrental.dto.order.OrderReturnRequest;
import com.lfz.carrental.entity.RentalOrder;
import com.lfz.carrental.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ApiResponse<IPage<RentalOrder>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status
    ) {
        return ApiResponse.success(orderService.listAdminOrders(page, size, status));
    }

    @PutMapping("/{id}/pickup")
    public ApiResponse<Void> pickup(@PathVariable Long id) {
        orderService.confirmPickup(id);
        return ApiResponse.success("已确认出车");
    }

    @PutMapping("/{id}/return")
    public ApiResponse<Void> returnAndSettle(@PathVariable Long id, @Valid @RequestBody OrderReturnRequest request) {
        orderService.confirmReturn(id, request);
        return ApiResponse.success("已完成还车结算");
    }
}

