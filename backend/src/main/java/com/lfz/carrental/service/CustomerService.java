package com.lfz.carrental.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lfz.carrental.common.ApiException;
import com.lfz.carrental.dto.customer.CustomerCreateRequest;
import com.lfz.carrental.dto.customer.CustomerUpdateRequest;
import com.lfz.carrental.entity.RentalOrder;
import com.lfz.carrental.entity.SysUser;
import com.lfz.carrental.mapper.RentalOrderMapper;
import com.lfz.carrental.mapper.SysUserMapper;
import com.lfz.carrental.vo.customer.CustomerVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private static final String USER_ROLE = "USER";

    private final SysUserMapper userMapper;
    private final RentalOrderMapper orderMapper;

    public IPage<CustomerVO> listCustomers(Integer page, Integer size, String keyword, Integer status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getDeleted, 0);
        wrapper.eq(SysUser::getRoleCode, USER_ROLE);
        wrapper.eq(status != null, SysUser::getStatus, status);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or()
                    .like(SysUser::getRealName, keyword)
                    .or()
                    .like(SysUser::getPhone, keyword));
        }
        wrapper.orderByDesc(SysUser::getCreatedAt);

        IPage<SysUser> userPage = userMapper.selectPage(new Page<>(page, size), wrapper);
        Page<CustomerVO> result = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        result.setRecords(userPage.getRecords().stream().map(this::toVO).toList());
        return result;
    }

    public CustomerVO getCustomer(Long id) {
        SysUser user = getUserCustomer(id);
        return toVO(user);
    }

    public CustomerVO create(CustomerCreateRequest request) {
        ensureUsernameUnique(request.getUsername(), null);
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        user.setRoleCode(USER_ROLE);
        userMapper.insert(user);
        return toVO(user);
    }

    public CustomerVO update(Long id, CustomerUpdateRequest request) {
        SysUser user = getUserCustomer(id);
        ensureUsernameUnique(request.getUsername(), id);

        user.setUsername(request.getUsername());
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(request.getPassword());
        }
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        userMapper.updateById(user);
        return toVO(user);
    }

    public void updateStatus(Long id, Integer status) {
        SysUser user = getUserCustomer(id);
        user.setStatus(status);
        userMapper.updateById(user);
    }

    public void delete(Long id) {
        SysUser user = getUserCustomer(id);
        Long activeOrderCount = orderMapper.selectCount(new LambdaQueryWrapper<RentalOrder>()
                .eq(RentalOrder::getUserId, user.getId())
                .eq(RentalOrder::getDeleted, 0)
                .in(RentalOrder::getOrderStatus, List.of(
                        OrderService.ORDER_WAIT_PAY,
                        OrderService.ORDER_WAIT_PICKUP,
                        OrderService.ORDER_RENTING
                )));
        if (activeOrderCount != null && activeOrderCount > 0) {
            throw new ApiException("该客户存在进行中订单，不能删除");
        }
        userMapper.deleteById(id);
    }

    private SysUser getUserCustomer(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null || user.getDeleted() != 0 || !USER_ROLE.equals(user.getRoleCode())) {
            throw new ApiException("客户不存在");
        }
        return user;
    }

    private void ensureUsernameUnique(String username, Long excludeId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        wrapper.eq(SysUser::getDeleted, 0);
        if (excludeId != null) {
            wrapper.ne(SysUser::getId, excludeId);
        }
        Long count = userMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new ApiException("用户名已存在");
        }
    }

    private CustomerVO toVO(SysUser user) {
        CustomerVO vo = new CustomerVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }
}

