package com.example.TextRecognitionService.Utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class VerificationCodeGenerator {
    public int generateCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // Генерация 6-значного числа
        return (code);
    }
}
