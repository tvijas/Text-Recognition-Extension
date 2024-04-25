package com.example.TextRecognitionService.Utils;

import com.example.TextRecognitionService.models.entity.UserEntity;
import com.example.TextRecognitionService.models.request.SignupRequest;
import com.example.TextRecognitionService.services.OauthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component @RequiredArgsConstructor
public class RegistrationValidator implements Validator {
    private final OauthService oauthService;

    @Override
    public boolean supports(Class<?> clazz) {
        return SignupRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (!(target instanceof SignupRequest)){
            errors.reject("SignupRequest","target is not SignupRequest");
        }
        SignupRequest request = (SignupRequest) target;
        if (oauthService.checkEmail(request.getEmail()) != null) {
            errors.rejectValue("email", "", "This email is already taken");
        }
        if (oauthService.checkLogin(request.getLogin()) != null) {
            errors.rejectValue("login", "", "This login is already taken");
        }
    }
}
