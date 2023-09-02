package com.haegreen.fishing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FishingApplication {

    public static void main(String[] args) {
        SpringApplication.run(FishingApplication.class, args);
    }

}
