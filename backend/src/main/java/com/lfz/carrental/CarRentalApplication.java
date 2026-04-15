package com.lfz.carrental;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lfz.carrental.mapper")
public class CarRentalApplication {

    // 项目启动入口
    public static void main(String[] args) {
        SpringApplication.run(CarRentalApplication.class, args);
    }
}
