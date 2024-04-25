package com.example.TextRecognitionService.controller;

import com.example.TextRecognitionService.Utils.LoginValidator;
import com.example.TextRecognitionService.Utils.RegistrationValidator;
import com.example.TextRecognitionService.Utils.VerificationCodeGenerator;
import com.example.TextRecognitionService.models.TokensAndErrors;
import com.example.TextRecognitionService.models.entity.UserEntity;
import com.example.TextRecognitionService.models.request.*;
import com.example.TextRecognitionService.models.response.*;
import com.example.TextRecognitionService.models.template.MailTemplate;
import com.example.TextRecognitionService.properties.JwtProps;
import com.example.TextRecognitionService.repositories.UserRepository;
import com.example.TextRecognitionService.services.EmailSenderService;
import com.example.TextRecognitionService.services.OauthService;
import com.example.TextRecognitionService.services.TextRecognitionService;
import com.example.TextRecognitionService.services.TextTranslationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TextRecognitionRestController {
    private final TextTranslationService textTranslationService;
    private final TextRecognitionService textRecognitionService;
    private final RegistrationValidator registrationValidator;
    private final EmailSenderService emailSenderService;
    private final VerificationCodeGenerator verificationCodeGenerator;
    private final OauthService oauthService;
    private final LoginValidator loginValidator;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtProps jwtProps;

    @PostMapping
    @RequestMapping(value = "/recognize", produces = "application/json;charset=UTF-8")
    public ResponseEntity<RecognizeResponse> detectText(@RequestBody RecognizeRequest recognizeRequest) {
        //Validation
        String accessToken = recognizeRequest.getAccessToken();
        Map<String, String> errors = oauthService.verifyAccessToken(accessToken);
        Integer id = oauthService.extractIdFromToken(accessToken);
        if (id == null)
            errors.put("IncorrectTokenClaimException", "id field is null or doesn't exists");
        if (recognizeRequest.getBase64Data() == null || recognizeRequest.getBase64Data().isBlank())
            errors.put("Base64NullPointerException", "base64Data is Blank");
        if (!errors.isEmpty())
            return new ResponseEntity<>(new RecognizeResponse(null, null, errors), HttpStatusCode.valueOf(HttpStatus.SC_BAD_REQUEST));
        //increase amount of requests by one If validation is successful
        oauthService.addAmountOfRequestsById(id.intValue());
        String recognizedText = textRecognitionService.detectText(recognizeRequest.getBase64Data());
        String translatedText = null;
        //Checking if translation is needed
        if (!recognizeRequest.getTargetLanguage().isBlank())
            translatedText = textTranslationService.translateText(recognizedText, recognizeRequest.getTargetLanguage());
        //Returning translated and recognized text
        return new ResponseEntity<>(new RecognizeResponse(recognizedText, translatedText, errors), HttpStatusCode.valueOf(HttpStatus.SC_OK));
    }

    @PatchMapping
    @RequestMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@RequestBody RefreshRequest request) {
        //Validation
        TokensAndErrors tokensAndErrors = oauthService.refreshRefreshAndAccessToken
                (request.getRefreshToken(), request.getAccessToken());

        if (tokensAndErrors.getTokens() == null)
            return new ResponseEntity<>(new RefreshResponse(tokensAndErrors.getErrors(), null, null),
                    HttpStatusCode.valueOf(HttpStatus.SC_BAD_REQUEST));
        //Returning new tokens if validation is successful
        return new ResponseEntity<>(new RefreshResponse(tokensAndErrors.getErrors(),
                tokensAndErrors.getTokens().get("access_token"),
                tokensAndErrors.getTokens().get("refresh_token")
        ), HttpStatusCode.valueOf(HttpStatus.SC_ACCEPTED));
    }

    @PostMapping
    @RequestMapping("/signup")
    public ResponseEntity<SignupResponse> signupUser(@Valid @RequestBody SignupRequest request, BindingResult bindingResult) {
        //Validation
        registrationValidator.validate(request, bindingResult);
        Map<String, String> errors = checkForErrors(bindingResult);

        if (!errors.isEmpty())
            return new ResponseEntity<>(new SignupResponse(errors), HttpStatusCode.valueOf(HttpStatus.SC_BAD_REQUEST));
        //Adding new user to db and sending confirmation code to the email
        UserEntity userEntity = new UserEntity(request.getEmail(), request.getLogin(), passwordEncoder.encode(request.getPassword()));
        MailTemplate mailTemplate = new MailTemplate();
        int vcode = verificationCodeGenerator.generateCode();
        mailTemplate.setStandardEmailTemplate(request.getEmail(), vcode);
        emailSenderService.sendEmail(mailTemplate);
        userEntity.setCode(vcode);
        userEntity.setCreationTime(Duration.ofMillis(jwtProps.getAccessTokenDuration()));
        oauthService.save(userEntity);
        return new ResponseEntity<>(new SignupResponse(errors), HttpStatusCode.valueOf(HttpStatus.SC_OK));
    }

    @PostMapping
    @RequestMapping("/confirm")
    public ResponseEntity<VerificationResponse> verificationRequest(@Valid @RequestBody VerificationRequest vr, BindingResult bindingResult) {
        Map<String, String> errors = checkForErrors(bindingResult);
        if (errors.isEmpty()) {
            Optional<UserEntity> user = userRepository.findByEmailAndCode(vr.getEmail(), vr.getCode());
            if (user.isPresent()) {
                UserEntity userEntity = user.get();
                if (!userEntity.getExpirationTime().isBefore(LocalDateTime.now())) {
                    oauthService.confirmUser(vr.getEmail(), vr.getCode(), LocalDateTime.now());
                    return new ResponseEntity<>(new VerificationResponse(errors), HttpStatusCode.valueOf(HttpStatus.SC_OK));
                } else {
                    errors.put("CodeExpiredException", "verification code has been expired");
                    return new ResponseEntity<>(new VerificationResponse(errors), HttpStatusCode.valueOf(HttpStatus.SC_BAD_REQUEST));
                }
            } else {
                errors.put("EmailAndCodeNotFoundException", "email and code were not found");
                return new ResponseEntity<>(new VerificationResponse(errors), HttpStatusCode.valueOf(HttpStatus.SC_BAD_REQUEST));
            }
        } else {
            return new ResponseEntity<>(new VerificationResponse(errors), HttpStatusCode.valueOf(HttpStatus.SC_BAD_REQUEST));
        }
    }

    @PostMapping
    @RequestMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, BindingResult bindingResult, HttpServletResponse response) {
        //Validation
        loginValidator.validate(request, bindingResult);
        Map<String, String> errors = checkForErrors(bindingResult);
        if (!errors.isEmpty())
            return new ResponseEntity<>(new LoginResponse(errors, null, null),
                    HttpStatusCode.valueOf(HttpStatus.SC_NOT_FOUND));
        //Creating new tokens and returning them
        String[] refreshAndAccessToken = oauthService.createRefreshAndAccessToken(request.getLogin());
        return new ResponseEntity<>(new LoginResponse(errors,
                refreshAndAccessToken[1],
                refreshAndAccessToken[0]),
                HttpStatusCode.valueOf(HttpStatus.SC_ACCEPTED));

    }

    @PostMapping
    @RequestMapping("/resendCode")
    public ResponseEntity<ResendCodeResponse> resendCode(@RequestBody String email) throws JsonProcessingException {
        //Reading email from the body
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(email);
        email = jsonNode.get("email").asText();

        Map<String, String> errors = new HashMap<>();
        UserEntity userEntity = oauthService.checkEmail(email);
        //Validation
        if (userEntity == null) {
            errors.put("EmailNotFoundException", "User with such email not found");
            return new ResponseEntity<>(new ResendCodeResponse(errors),
                    HttpStatusCode.valueOf(HttpStatus.SC_NOT_FOUND));
        }
        //Sending new code to the email
        MailTemplate mailTemplate = new MailTemplate();
        int vcode = verificationCodeGenerator.generateCode();
        mailTemplate.setStandardEmailTemplate(email, vcode);
        emailSenderService.sendEmail(mailTemplate);
        userEntity.setCode(vcode);
        userEntity.setCreationTime(Duration.ofMillis(jwtProps.getAccessTokenDuration()));
        oauthService.save(userEntity);
        return new ResponseEntity<>(new ResendCodeResponse(errors),
                HttpStatusCode.valueOf(HttpStatus.SC_OK));
    }

    @PostMapping
    @RequestMapping("/resetPassword")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request, BindingResult bindingResult) {
        //Validation
        Map<String, String> errors = checkForErrors(bindingResult);
        if (!errors.isEmpty())
            return new ResponseEntity<>(new ResetPasswordResponse(errors),
                    HttpStatusCode.valueOf(HttpStatus.SC_BAD_REQUEST));
        //Trying to reset password
        try {
            oauthService.resetPassword(passwordEncoder.encode(request.getPassword()), request.getEmail(), request.getCode());
            return new ResponseEntity<>(new ResetPasswordResponse(errors), HttpStatusCode.valueOf(HttpStatus.SC_OK));
        } catch (Exception e) {
            e.printStackTrace();
            errors.put("VerificationCodeNotFoundException", "Verification code is incorrect or expired");
            return new ResponseEntity<>(new ResetPasswordResponse(errors),
                    HttpStatusCode.valueOf(HttpStatus.SC_NOT_FOUND));
        }
    }
    //This method checks BindingResult for errors and returns them as Map
    public Map<String, String> checkForErrors(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        if (bindingResult.hasErrors()) {
            for (FieldError error : bindingResult.getFieldErrors()) {
                String fieldName = error.getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            }
        }
        return errors;
    }
}
