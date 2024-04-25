package com.example.TextRecognitionService.repositories;

import com.example.TextRecognitionService.models.entity.UserEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByLogin(String login);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByIdAndLoginAndRefreshTokenAndRefreshNum(int id, String login, String refreshToken, int refreshNum);

    Optional<UserEntity> findByEmailAndCode(String email, int code);
    @Modifying
    @Query("UPDATE user_data e SET e.registrationFinished = true WHERE e.email = :email AND e.code = :code AND e.expirationTime > :localDateTime")
    int confirmUser(@Param("email") String email, @Param("code") int code, @Param("localDateTime") LocalDateTime localDateTime);

    @Modifying
    @Query("UPDATE user_data e SET e.password = :password WHERE  e.email = :email AND e.code = :code AND e.expirationTime > :localDateTime")
    int resetPassword(@Param("password") String password,
                      @Param("email") String email,
                      @Param("code") int code,
                      @Param("localDateTime") LocalDateTime localDateTime);
    @Modifying
    @Query("update user_data e set e.amountOfRequests = e.amountOfRequests + 1 where e.id = :id")
    void addAmountOfRequests(@Param("id") int id);
}
