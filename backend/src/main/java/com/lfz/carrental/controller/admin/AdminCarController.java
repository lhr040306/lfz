package com.lfz.carrental.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lfz.carrental.common.ApiResponse;
import com.lfz.carrental.dto.car.CarCreateRequest;
import com.lfz.carrental.dto.car.CarStatusRequest;
import com.lfz.carrental.dto.car.CarUpdateRequest;
import com.lfz.carrental.entity.CarInfo;
import com.lfz.carrental.service.CarService;
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
@RequestMapping("/api/v1/admin/cars")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCarController {

    private final CarService carService;

    @GetMapping
    public ApiResponse<IPage<CarInfo>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Integer status
    ) {
        return ApiResponse.success(carService.listAdminCars(page, size, brand, status));
    }

    @PostMapping
    public ApiResponse<CarInfo> create(@Valid @RequestBody CarCreateRequest request) {
        return ApiResponse.success(carService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CarInfo> update(@PathVariable Long id, @Valid @RequestBody CarUpdateRequest request) {
        return ApiResponse.success(carService.update(id, request));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody CarStatusRequest request) {
        carService.updateStatus(id, request.getStatus());
        return ApiResponse.success("状态已更新");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        carService.deleteCar(id);
        return ApiResponse.success("车辆已删除");
    }
}
