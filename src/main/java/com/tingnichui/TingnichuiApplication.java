package com.tingnichui;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@MapperScan("com.tingnichui.dao")
@SpringBootApplication
public class TingnichuiApplication {

    public static void main(String[] args) {
//        SpringApplicationBuilder builder = new SpringApplicationBuilder(ChunhuitradeApplication.class);
//        builder.headless(false).run(args);


        System.setProperty("java.awt.headless", "false");

        SpringApplication.run(TingnichuiApplication.class, args);
    }

}
