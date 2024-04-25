package com.example.TextRecognitionService.models.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class ResendCodeResponse {
    private Map<String,String> errors;
    public ResendCodeResponse (@JsonProperty("errors") Map<String,String> errors){
        this.errors = errors;
    }
}
