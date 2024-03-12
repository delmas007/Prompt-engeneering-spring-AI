package com.example.promptengineeringai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:Key/.env")
public class PromptEngineeringAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PromptEngineeringAiApplication.class, args);
    }

}
