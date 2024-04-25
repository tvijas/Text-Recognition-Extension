package com.example.TextRecognitionService.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResetPasswordRequest {
    @Valid
    @NotBlank (message = "Email field is empty")
    @Email (message = "Isn't email")
    private String email;
    @JsonProperty("verification_code")
    private int code;
    @Valid
    @NotBlank(message = "Password field shouldn't be empty")
    @Size(min = 9, max = 20, message = "Password size should be from 9 to 20 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&_-])[A-Za-z\\d@$!%*#?&_-]+$", message = "Password must contain at least one uppercase letter, lowercase letter and special digit")
    private String password;
}
