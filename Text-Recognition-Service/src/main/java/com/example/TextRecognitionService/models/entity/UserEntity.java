package com.example.TextRecognitionService.models.entity;

import com.example.TextRecognitionService.properties.JwtProps;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.context.annotation.PropertySource;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity(name = "user_data")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false, unique = true)
    private String login;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(name = "registration_finished", columnDefinition = "INT DEFAULT 0")
    private boolean registrationFinished;
    @Column(name = "amount_of_requests", columnDefinition = "INT DEFAULT 0")
    private int amountOfRequests;
    @Column(name = "refresh_token")
    private String refreshToken;
    @Column(name = "refresh_num")
    private int refreshNum;
    @Column(name = "verification_code")
    private int code;
    @Column(name = "expiration_time")
    private LocalDateTime expirationTime;

    public UserEntity(String email, String login, String password) {
        this.login = login;
        this.email = email;
        this.password = password;
    }
    public boolean isRegistrationFinished(){
        return this.registrationFinished;
    }
    public void setCreationTime(Duration duration) {
        this.expirationTime = LocalDateTime.now().plus(duration);

    }
}
