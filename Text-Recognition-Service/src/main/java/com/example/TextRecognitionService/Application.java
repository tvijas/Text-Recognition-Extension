package com.example.TextRecognitionService;

import com.example.TextRecognitionService.properties.DataBaseProps;
import com.example.TextRecognitionService.properties.EmailSenderProps;
import com.example.TextRecognitionService.properties.JwtProps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


@SpringBootApplication
@EnableConfigurationProperties({JwtProps.class, EmailSenderProps.class, DataBaseProps.class})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
