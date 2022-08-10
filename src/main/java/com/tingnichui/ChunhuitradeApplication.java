package com.tingnichui;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@MapperScan("com.tingnichui.dao")
@SpringBootApplication
public class ChunhuitradeApplication {

    public static void main(String[] args) {
//        SpringApplicationBuilder builder = new SpringApplicationBuilder(ChunhuitradeApplication.class);
//        builder.headless(false).run(args);


        System.setProperty("java.awt.headless", "false");

        SpringApplication.run(ChunhuitradeApplication.class, args);
    }

}
