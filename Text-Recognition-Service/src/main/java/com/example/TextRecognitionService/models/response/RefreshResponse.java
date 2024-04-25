package com.example.TextRecognitionService.models.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class RefreshResponse {
    private String refreshToken;
    private String accessToken;
    private Map<String,String> errors;
    @JsonCreator
    public RefreshResponse(@JsonProperty("errors") Map<String, String> errors,
                           @JsonProperty("access_token") String accessToken,
                           @JsonProperty("refresh_token") String refreshToken) {
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
        this.errors = errors;
    }
}
