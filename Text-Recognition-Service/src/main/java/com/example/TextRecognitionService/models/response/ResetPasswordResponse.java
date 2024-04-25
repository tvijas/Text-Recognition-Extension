package com.example.TextRecognitionService.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class ResetPasswordResponse {
    private Map<String,String> errors;
    public ResetPasswordResponse (@JsonProperty("errors") Map<String,String> errors){
        this.errors = errors;
    }
}
