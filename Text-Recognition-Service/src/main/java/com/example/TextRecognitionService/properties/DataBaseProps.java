package com.example.TextRecognitionService.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "db")
public class DataBaseProps {
    private String host;
    private String port;
    private String name;
    private String username;
    private String password;
}
