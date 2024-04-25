package com.example.TextRecognitionService.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class VerificationRequest {
    @Valid
    @NotBlank (message = "Email field is empty")
    @Email (message = "Isn't email")
    private String email;
    @JsonProperty("verification_code")
    private int code;
}
