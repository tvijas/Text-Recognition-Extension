package com.example.TextRecognitionService10;

import com.example.TextRecognitionService.Application;
import com.example.TextRecognitionService.models.entity.UserEntity;
import com.example.TextRecognitionService.models.request.*;
import com.example.TextRecognitionService.models.response.*;
import com.example.TextRecognitionService.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class}, properties = "spring.config.location=classpath:test.properties")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApplicationTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Value("${test.email}")
    private String testEmail;
    private UserEntity userEntity;
    @Getter
    @Setter
    private static String accessToken;
    @Getter
    @Setter
    private static String refreshToken;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EntityManager entityManager;

    @Test
    @Order(1)
    @Commit
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void testSignupUserSuccess() throws Exception {
        SignupRequest signupRequest = new SignupRequest(testEmail, "someuser", "Aa-#115663");

        MvcResult result = mockMvc.perform(post("/api/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(signupRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            SignupResponse signupResponse = objectMapper.readValue(responseContent, SignupResponse.class);

            Assertions.assertTrue(signupResponse.getErrors().isEmpty());

            Optional<UserEntity> optionalUser = userRepository.findByLogin("someuser");

            Assertions.assertTrue(optionalUser.isPresent());
            if (optionalUser.isPresent()) {
                userEntity = optionalUser.get();
            }
            userEntity = userRepository.findByLogin("someuser").get();
        } catch (IOException e) {
            e.printStackTrace();
            Assertions.fail("Error parsing JSON response");
        }
    }

    @Test
    @Order(2)
    void testSignupUserValidationFailure() throws Exception {
        SignupRequest signupRequest = new SignupRequest("tvijasssgmail.com", "som", "123");

        MvcResult result = mockMvc.perform(post("/api/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(responseContent);
        try {
            SignupResponse signupResponse = objectMapper.readValue(responseContent, SignupResponse.class);
            System.out.println(signupResponse.getErrors());
            Assertions.assertNotNull(signupResponse.getErrors());
            Assertions.assertFalse(signupResponse.getErrors().isEmpty());
        } catch (IOException e) {
            e.printStackTrace();
            Assertions.fail("Error parsing JSON response");
        }
    }

    @Test
    @Order(3)
    @Commit
    @Transactional
    void testConfirmationSuccess() throws Exception {
        Optional<UserEntity> optionalUser = userRepository.findByLogin("someuser");
        if (optionalUser.isPresent()) {
            userEntity = optionalUser.get();
        }
        userEntity = userRepository.findByLogin("someuser").get();
        VerificationRequest verificationRequest = new VerificationRequest(testEmail, userEntity.getCode());

        MvcResult result = mockMvc.perform(post("/api/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(verificationRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            System.out.println(responseContent);
            VerificationResponse verificationResponse = objectMapper.readValue(responseContent, VerificationResponse.class);

            Assertions.assertTrue(verificationResponse.getErrors().isEmpty());
        } catch (IOException e) {
            e.printStackTrace();
            Assertions.fail("Error parsing JSON response");
        }
    }

    @Test
    @Order(4)
    void testConfirmationFailure_IncorrectCode() throws Exception {
        VerificationRequest verificationRequest = new VerificationRequest(testEmail, 12513);

        MvcResult result = mockMvc.perform(post("/api/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(verificationRequest)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            VerificationResponse verificationResponse = objectMapper.readValue(responseContent, VerificationResponse.class);

            Assertions.assertNotNull(verificationResponse.getErrors());
        } catch (IOException e) {
            e.printStackTrace();
            Assertions.fail("Error parsing JSON response");
        }
    }

    @Test
    @Order(5)
    @Commit
    @Transactional
    void testLoginSuccess() throws Exception {
        LoginRequest loginRequest = new LoginRequest("someuser", "Aa-#115663");

        MvcResult result = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(loginRequest)))
                .andExpect(status().isAccepted())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            LoginResponse loginResponse = objectMapper.readValue(responseContent, LoginResponse.class);

            Assertions.assertTrue(loginResponse.getErrors().isEmpty());

            setAccessToken(loginResponse.getAccessToken());
            setRefreshToken(loginResponse.getRefreshToken());

            Assertions.assertNotNull(loginResponse.getAccessToken());
            Assertions.assertNotNull(loginResponse.getRefreshToken());

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Error parsing JSON");
        }
    }

    @Test
    @Order(6)
    void testLoginWithFailure_ValidationAndExistingUser() throws Exception {
        LoginRequest loginRequest = new LoginRequest("ser", "Aa663");

        MvcResult result = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(loginRequest)))
                .andExpect(status().isNotFound())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            LoginResponse loginResponse = objectMapper.readValue(responseContent, LoginResponse.class);

            Assertions.assertFalse(loginResponse.getErrors().isEmpty());

            Assertions.assertNull(loginResponse.getAccessToken());
            Assertions.assertNull(loginResponse.getRefreshToken());
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Error parsing JSON");
        }
    }

    @Test
    @Order(7)
    @Commit
    @Transactional
    void testRecognizeSuccess() throws Exception {
        RecognizeRequest recognizeRequest = new RecognizeRequest(getAccessToken(), imageToBase64(), "ru");

        MvcResult result = mockMvc.perform(post("/api/recognize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(recognizeRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            RecognizeResponse recognizeResponse = objectMapper.readValue(responseContent, RecognizeResponse.class);
            System.out.println("recognizedText: " + recognizeResponse.getRecognizedText());
            System.out.println("translatedText: " + recognizeResponse.getTranslatedText());

            Assertions.assertTrue(recognizeResponse.getErrors().isEmpty());



            Assertions.assertEquals("Typo: In word 'someuser' :61", recognizeResponse.getRecognizedText());
            Assertions.assertEquals("Опечатка: В слове «someuser» :61", recognizeResponse.getTranslatedText());

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Error parsing JSON");
        }
    }

    @Test
    @Order(8)
    void testRecognizeFailure_AccessTokenIsNull() throws Exception {
        RecognizeRequest recognizeRequest = new RecognizeRequest(null, imageToBase64(), "ru");

        MvcResult result = mockMvc.perform(post("/api/recognize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(recognizeRequest)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            RecognizeResponse recognizeResponse = objectMapper.readValue(responseContent, RecognizeResponse.class);

            Assertions.assertNull(recognizeResponse.getRecognizedText());
            Assertions.assertNull(recognizeResponse.getTranslatedText());
            Assertions.assertFalse(recognizeResponse.getErrors().isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Error parsing JSON");
        }
    }

    @Test
    @Order(9)
    void testResendCodeSuccess() throws Exception {
        Optional<UserEntity> userOptional1 = userRepository.findByEmail(testEmail);

        MvcResult result = mockMvc.perform(post("/api/resendCode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"email\": " + asJsonString(testEmail) + " }"))
                .andExpect(status().isOk())
                .andReturn();

        Optional<UserEntity> userOptional2 = userRepository.findByEmail(testEmail);

        String responseContent = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            ResendCodeResponse response = objectMapper.readValue(responseContent, ResendCodeResponse.class);
            Assertions.assertTrue(response.getErrors().isEmpty());
            Assertions.assertTrue(userOptional1.get().getCode() != userOptional2.get().getCode());
            Assertions.assertNotSame(userOptional1.get().getExpirationTime(), userOptional2.get().getExpirationTime());
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Error parsing JSON");
        }
    }

    @Test
    @Order(10)
    void testResendCodeFailure_NotExistingUserEmail() throws Exception {
        Optional<UserEntity> userOptional1 = userRepository.findByEmail(testEmail);

        MvcResult result = mockMvc.perform(post("/api/resendCode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"email\": " + asJsonString("151fsasfas") + " }"))
                .andExpect(status().isNotFound())
                .andReturn();

        Optional<UserEntity> userOptional2 = userRepository.findByEmail(testEmail);

        String responseContent = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ResendCodeResponse response = objectMapper.readValue(responseContent, ResendCodeResponse.class);
            Assertions.assertFalse(response.getErrors().isEmpty());
            Assertions.assertEquals(userOptional1.get().getCode(), userOptional2.get().getCode());
            Assertions.assertEquals(userOptional1.get().getExpirationTime(), userOptional2.get().getExpirationTime());
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Error parsing JSON");
        }
    }

    @Test
    @Order(12)
    @Commit
    @Transactional
    void testResetPasswordSuccess() throws Exception {
        Optional<UserEntity> userOptional1 = userRepository.findByEmail(testEmail);

        userEntity = userOptional1.get();
        UserEntity user = userOptional1.get();
        user.setExpirationTime(LocalDateTime.now().plusMinutes(60));
        userRepository.saveAndFlush(user);

        ResetPasswordRequest request = new ResetPasswordRequest(testEmail, userOptional1.get().getCode(), "Aa-#115663lox");

        MvcResult result = mockMvc.perform(post("/api/resetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andReturn();

        entityManager.clear();

        Optional<UserEntity> userOptional2 = userRepository.findByEmail(testEmail);

        String responseContent = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            ResetPasswordResponse response = objectMapper.readValue(responseContent, ResetPasswordResponse.class);
            Assertions.assertTrue(response.getErrors().isEmpty());
            Assertions.assertTrue(passwordEncoder.matches("Aa-#115663lox", userOptional2.get().getPassword()));
            Assertions.assertFalse(passwordEncoder.matches("Aa-#115663", userOptional2.get().getPassword()));

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Error parsing JSON");
        }
    }

    @Test
    @Order(11)
    void testResetPasswordFailure_CodeIsExpired() throws Exception {
        Optional<UserEntity> userOptional1 = userRepository.findByEmail(testEmail);

        userEntity = userOptional1.get();
        UserEntity user = userOptional1.get();
        user.setExpirationTime(LocalDateTime.now().minusMinutes(61));
        userRepository.saveAndFlush(user);

        ResetPasswordRequest request = new ResetPasswordRequest(testEmail, userOptional1.get().getCode(), "Aa-#115663lox");

        MvcResult result = mockMvc.perform(post("/api/resetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isNotFound())
                .andReturn();

        entityManager.clear();

        Optional<UserEntity> userOptional2 = userRepository.findByEmail(testEmail);

        String responseContent = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            ResetPasswordResponse response = objectMapper.readValue(responseContent, ResetPasswordResponse.class);
            Assertions.assertFalse(response.getErrors().isEmpty());
            Assertions.assertTrue(passwordEncoder.matches("Aa-#115663", userOptional2.get().getPassword()));
            Assertions.assertFalse(passwordEncoder.matches("Aa-#115663lox", userOptional2.get().getPassword()));
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Error parsing JSON");
        }
    }


    private String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String imageToBase64() {
        try {
            // Чтение изображения из
            InputStream inputStream = Application.class.getResourceAsStream("/img.png");
            if (inputStream == null) {
                throw new IOException("File not found");
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            // Кодирование изображения в Base64
            byte[] imageData = byteArrayOutputStream.toByteArray();
            String base64Data = Base64.getEncoder().encodeToString(imageData);

            // Закрытие потоков
            inputStream.close();
            byteArrayOutputStream.close();

            return base64Data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
