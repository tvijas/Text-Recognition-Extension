package com.example.TextRecognitionService.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties
public class JwtProps {
    @Value("${access.token.mls}")
    private int accessTokenDuration;
    @Value("${refresh.token.mls}")
    private int refreshTokenDuration;
    @Value("${secret}")
    private String secret;
}
