package com.example.TextRecognitionService.Utils;

import com.example.TextRecognitionService.models.entity.UserEntity;
import com.example.TextRecognitionService.models.request.LoginRequest;
import com.example.TextRecognitionService.services.OauthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class LoginValidator implements Validator {
    private final PasswordEncoder passwordEncoder;
    private final OauthService oauthService;

    @Override
    public boolean supports(Class<?> clazz) {
        return LoginRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (!(target instanceof LoginRequest)) {
            errors.reject("LoginRequest", "target is not LoginRequest");
        }
        LoginRequest request = (LoginRequest) target;
        UserEntity userEntity = oauthService.checkLogin(request.getLogin());
        if (userEntity == null || !passwordEncoder.matches(request.getPassword(), userEntity.getPassword())) {
            errors.rejectValue("login", "", "login or password is incorrect");
            return;
        }
        if (!userEntity.isRegistrationFinished()) {
            errors.reject("registrationFinished", "email isn't confirmed");
        }

    }
}
