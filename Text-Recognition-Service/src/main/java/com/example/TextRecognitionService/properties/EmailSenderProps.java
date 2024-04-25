package com.example.TextRecognitionService.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data @ConfigurationProperties(prefix = "spring.mail")
public class EmailSenderProps {
    private String host;
    private int port;
    private String username;
    private String password;
}
