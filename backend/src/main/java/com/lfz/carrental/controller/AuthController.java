package com.lfz.carrental.controller;

import com.lfz.carrental.common.ApiResponse;
import com.lfz.carrental.common.SecurityUtils;
import com.lfz.carrental.dto.auth.LoginRequest;
import com.lfz.carrental.dto.auth.LoginResponse;
import com.lfz.carrental.dto.auth.RegisterRequest;
import com.lfz.carrental.service.AuthService;
import com.lfz.carrental.vo.UserProfileVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.success("注册成功");
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/profile")
    public ApiResponse<UserProfileVO> profile() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(authService.getProfile(userId));
    }
}

