package com.lfz.carrental.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lfz.carrental.entity.CarInfo;
import com.lfz.carrental.entity.SysUser;
import com.lfz.carrental.mapper.CarInfoMapper;
import com.lfz.carrental.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final CarInfoMapper carInfoMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initAdmin();
        initCars();
    }

    private void initAdmin() {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, "admin")
                .eq(SysUser::getDeleted, 0));
        if (count != null && count > 0) {
            return;
        }
        SysUser admin = new SysUser();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRealName("系统管理员");
        admin.setPhone("13800000000");
        admin.setStatus(1);
        admin.setRoleCode("ADMIN");
        userMapper.insert(admin);
    }

    private void initCars() {
        Long count = carInfoMapper.selectCount(new LambdaQueryWrapper<CarInfo>()
                .eq(CarInfo::getDeleted, 0));
        if (count != null && count > 0) {
            return;
        }

        CarInfo car1 = new CarInfo();
        car1.setBrand("Toyota");
        car1.setSeries("Corolla");
        car1.setModel("2024 1.8L Hybrid");
        car1.setPlateNo("沪A12345");
        car1.setSeatCount(5);
        car1.setGearbox("自动");
        car1.setFuelType("油电混合");
        car1.setDayRent(new BigDecimal("268.00"));
        car1.setDeposit(new BigDecimal("2000.00"));
        car1.setStatus(1);
        car1.setMileage(13000);
        car1.setCoverUrl("https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=1200&q=60");
        carInfoMapper.insert(car1);

        CarInfo car2 = new CarInfo();
        car2.setBrand("Tesla");
        car2.setSeries("Model 3");
        car2.setModel("2023 后轮驱动版");
        car2.setPlateNo("沪B67890");
        car2.setSeatCount(5);
        car2.setGearbox("自动");
        car2.setFuelType("纯电");
        car2.setDayRent(new BigDecimal("498.00"));
        car2.setDeposit(new BigDecimal("3000.00"));
        car2.setStatus(1);
        car2.setMileage(9000);
        car2.setCoverUrl("https://images.unsplash.com/photo-1619767886558-efdc259cde1a?auto=format&fit=crop&w=1200&q=60");
        carInfoMapper.insert(car2);
    }
}

