package com.lfz.carrental.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lfz.carrental.common.ApiException;
import com.lfz.carrental.dto.auth.LoginRequest;
import com.lfz.carrental.dto.auth.LoginResponse;
import com.lfz.carrental.dto.auth.RegisterRequest;
import com.lfz.carrental.entity.SysUser;
import com.lfz.carrental.mapper.SysUserMapper;
import com.lfz.carrental.security.JwtTokenProvider;
import com.lfz.carrental.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;

    // 用户注册：按毕设要求，密码明文入库
    public void register(RegisterRequest request) {
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername())
                .eq(SysUser::getDeleted, 0));
        if (exists != null && exists > 0) {
            throw new ApiException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setPhone(request.getPhone());
        user.setRealName(request.getRealName());
        user.setStatus(1);
        user.setRoleCode("USER");
        userMapper.insert(user);
    }

    // 用户登录：明文密码直接比对
    public LoginResponse login(LoginRequest request) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername())
                .eq(SysUser::getDeleted, 0));
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new ApiException("用户不存在或已被禁用");
        }
        if (!request.getPassword().equals(user.getPassword())) {
            throw new ApiException("用户名或密码错误");
        }

        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername(), user.getRoleCode());
        return new LoginResponse(token, user.getUsername(), user.getRoleCode());
    }

    public UserProfileVO getProfile(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() != 0) {
            throw new ApiException("用户不存在");
        }

        UserProfileVO profile = new UserProfileVO();
        profile.setId(user.getId());
        profile.setUsername(user.getUsername());
        profile.setRealName(user.getRealName());
        profile.setPhone(user.getPhone());
        profile.setRoleCode(user.getRoleCode());
        return profile;
    }
}

