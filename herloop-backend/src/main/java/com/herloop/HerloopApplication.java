package com.herloop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class HerloopApplication {

    public static void main(String[] args) {
        // 强制 JVM 使用中国时区，确保 LocalDateTime.now() 返回北京时间
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(HerloopApplication.class, args);
    }
}
