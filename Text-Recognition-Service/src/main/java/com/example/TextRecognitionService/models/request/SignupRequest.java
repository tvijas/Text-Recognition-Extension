package com.example.TextRecognitionService.models.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignupRequest {

    @Valid
    @Email(message = "Isn't email")
    @NotBlank(message = "Email field can't be empty")
    @Size(max = 40, message = "Email is too long")
    private final String email;

    @Valid
    @NotBlank(message = "Login field shouldn't be empty")
    @Size(min = 6, max = 15, message = "Login size should be from 6 to 15 characters")
    private final String login;

    @Valid
    @NotBlank(message = "Password field shouldn't be empty")
    @Size(min = 9, max = 20, message = "Password size should be from 9 to 20 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&_-])[A-Za-z\\d@$!%*#?&_-]+$", message = "Password must contain at least one uppercase letter, lowercase letter and special digit")
    private final String password;
}
