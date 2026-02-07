package com.project.solvesync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SolveSyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(SolveSyncApplication.class, args);
    }

}
