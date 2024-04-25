package com.example.TextRecognitionService.models.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

    @Data
    public class RecognizeResponse {
        private String recognizedText;
        private String translatedText;
        private Map<String, String> errors;
        @JsonCreator
        public RecognizeResponse(@JsonProperty("recognized_text") String recognizedText,
                                 @JsonProperty("translated_text") String translatedText,
                                 @JsonProperty("errors") Map<String, String> errors) {
            this.recognizedText = recognizedText;
            this.translatedText = translatedText;
            this.errors = errors;
        }
    }
