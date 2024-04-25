package com.example.TextRecognitionService.models.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
public class VerificationResponse {
    private Map<String, String> errors;
    @JsonCreator
    public VerificationResponse(@JsonProperty("errors") Map<String, String> errors) {
        this.errors = errors;
    }
}
