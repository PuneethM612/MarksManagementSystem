package com.example.marksmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ImportResource("classpath:META-INF/beans.xml")
@EnableScheduling
public class MarksManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarksManagementApplication.class, args);
    }
} 