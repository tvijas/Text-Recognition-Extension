package com.example.TextRecognitionService.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.example.TextRecognitionService.Utils.VerificationCodeGenerator;
import com.example.TextRecognitionService.models.TokensAndErrors;
import com.example.TextRecognitionService.models.entity.UserEntity;
import com.example.TextRecognitionService.properties.JwtProps;
import com.example.TextRecognitionService.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OauthService {
    private final JwtProps jwtProps;
    private final UserRepository userRepository;
    private final VerificationCodeGenerator codeGenerator;


    public String[] createRefreshAndAccessToken(String login) {
        Optional<UserEntity> userEntity = userRepository.findByLogin(login);
        if (userEntity.isEmpty()) {
            return new String[]{null, null};
        }
        UserEntity user = userEntity.get();

        long expirationTime = System.currentTimeMillis() + jwtProps.getRefreshTokenDuration(); // 7 дней
        int accessNum = codeGenerator.generateCode();
        int refreshNum = codeGenerator.generateCode();
        Algorithm algorithm = Algorithm.HMAC256(jwtProps.getSecret());

        String refreshToken = JWT.create()
                .withClaim("id", user.getId())
                .withClaim("login", user.getLogin())
                .withClaim("accessNum", accessNum)
                .withClaim("refreshNum", refreshNum)
                .withExpiresAt(new Date(expirationTime))
                .sign(algorithm);

        user.setRefreshToken(refreshToken);
        user.setRefreshNum(refreshNum);
        userRepository.saveAndFlush(user);

        expirationTime = System.currentTimeMillis() + jwtProps.getAccessTokenDuration();
        String accessToken = JWT.create()
                .withClaim("id", user.getId())
                .withClaim("login", user.getLogin())
                .withClaim("accessNum", accessNum)
                .withExpiresAt(new Date(expirationTime))
                .sign(algorithm);

        return new String[]{refreshToken, accessToken};
    }

    public TokensAndErrors refreshRefreshAndAccessToken(String refreshToken, String accessToken) {
        Map<String, String> errors = new HashMap<>();
        errors.putAll(verifyToken(accessToken));
        errors.putAll(verifyToken(refreshToken));

        if (errors.isEmpty()) {

            DecodedJWT decodedAccessJWT = JWT.decode(accessToken);
            DecodedJWT decodedRefreshJWT = JWT.decode(refreshToken);

            int idR = decodedRefreshJWT.getClaim("id").asInt();
            int idA = decodedRefreshJWT.getClaim("id").asInt();

            String loginR = decodedRefreshJWT.getClaim("login").asString();
            String loginA = decodedAccessJWT.getClaim("login").asString();

            int accessNumR = decodedRefreshJWT.getClaim("accessNum").asInt();
            int accessNumA = decodedAccessJWT.getClaim("accessNum").asInt();

            int refreshNumR = decodedRefreshJWT.getClaim("refreshNum").asInt();

            if (idR == idA && Objects.equals(loginR, loginA) && accessNumR == accessNumA) {
                Optional<UserEntity> user = userRepository.findByIdAndLoginAndRefreshTokenAndRefreshNum(idR, loginR, refreshToken, refreshNumR);
                if (user.isPresent()) {
                    UserEntity userEntity = user.get();
                    String[] accessAndRefreshToken = createRefreshAndAccessToken(userEntity.getLogin());
                    Map<String, String> tokens = new HashMap<>();
                    tokens.put("refresh_token", accessAndRefreshToken[0]);
                    tokens.put("access_token", accessAndRefreshToken[1]);
                    return new TokensAndErrors(tokens, errors);
                }
            }
        }
        return new TokensAndErrors(null, errors);
    }

    // Пример верификации токена
    public Map<String, String> verifyToken(String token) {
        Map<String, String> errors = new HashMap<>();
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtProps.getSecret());
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return errors;
        } catch (JWTVerificationException e) {
            if (e.getCause() instanceof SignatureVerificationException) {
                errors.put("SignatureVerificationException", "signature is incorrect");
            } else if (e.getCause() instanceof TokenExpiredException) {
                errors.put("TokenExpiredException", "expired");
            } else {
                return errors;
            }
        }
        return null;
    }

    public Map<String, String> verifyAccessToken(String accessToken) {

        Map<String, String> errors = verifyToken(accessToken);
        if (accessToken == null || accessToken.isBlank()) {
            errors.put("TokenNullPointerException", "Token is null");
            return errors;
        }
        if (errors.isEmpty()) {
            DecodedJWT decodedJWT = JWT.decode(accessToken);
            int idA = decodedJWT.getClaim("id").asInt();
            String loginA = decodedJWT.getClaim("login").asString();
            Optional<UserEntity> user = userRepository.findById(idA);
            if (user.isPresent()) {
                UserEntity userEntity = user.get();
                if (compareTokenNums(userEntity.getRefreshToken(), accessToken)
                        && Objects.equals(userEntity.getLogin(), loginA)) return errors;
            } else {
                errors.put("IncorrectTokenClaim", "Access token has incorrect claim values");
                return errors;
            }
        }
        return errors;
    }

    private boolean compareTokenNums(String refreshToken, String accessToken) {
        DecodedJWT decodedRefreshJWT = JWT.decode(refreshToken);
        DecodedJWT decodedAccessJWT = JWT.decode(accessToken);
        return Objects.equals(decodedAccessJWT.getClaim("accessNum").asInt(),
                decodedRefreshJWT.getClaim("accessNum").asInt());
    }

    public Integer extractIdFromToken(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            return decodedJWT.getClaim("id").asInt();
        } catch (JWTDecodeException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Transactional
    public UserEntity checkLogin(String login) {
        Optional<UserEntity> user = userRepository.findByLogin(login);
        return user.orElse(null);
    }

    @Transactional
    public UserEntity checkEmail(String email) {
        Optional<UserEntity> user = userRepository.findByEmail(email);
        return user.orElse(null);
    }

    @Transactional
    public void save(UserEntity userEntity) {
        userRepository.saveAndFlush(userEntity);
    }
    @Transactional
    public void resetPassword(String password, String email, int code){
        int row = userRepository.resetPassword(password, email, code, LocalDateTime.now());
        if(row != 1){
            throw new IllegalArgumentException();
        }
    }
    @Transactional
    public void confirmUser(String email, int code, LocalDateTime localDateTime) {
        int row = userRepository.confirmUser(email, code, localDateTime);
        if (row != 1) {
            throw new IllegalArgumentException();
        }
    }
    @Transactional
    public void addAmountOfRequestsById(int id){
        userRepository.addAmountOfRequests(id);
    }
}
