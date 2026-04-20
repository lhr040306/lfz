package com.lfz.carrental.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lfz.carrental.common.ApiResponse;
import com.lfz.carrental.dto.customer.CustomerCreateRequest;
import com.lfz.carrental.dto.customer.CustomerStatusRequest;
import com.lfz.carrental.dto.customer.CustomerUpdateRequest;
import com.lfz.carrental.service.CustomerService;
import com.lfz.carrental.vo.customer.CustomerVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/customers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ApiResponse<IPage<CustomerVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status
    ) {
        return ApiResponse.success(customerService.listCustomers(page, size, keyword, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerVO> detail(@PathVariable Long id) {
        return ApiResponse.success(customerService.getCustomer(id));
    }

    @PostMapping
    public ApiResponse<CustomerVO> create(@Valid @RequestBody CustomerCreateRequest request) {
        return ApiResponse.success(customerService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CustomerVO> update(@PathVariable Long id, @Valid @RequestBody CustomerUpdateRequest request) {
        return ApiResponse.success(customerService.update(id, request));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody CustomerStatusRequest request) {
        customerService.updateStatus(id, request.getStatus());
        return ApiResponse.success("状态已更新");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ApiResponse.success("客户已删除");
    }
}

