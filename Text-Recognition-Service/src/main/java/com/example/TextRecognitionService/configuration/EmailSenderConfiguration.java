package com.example.TextRecognitionService.configuration;

import com.example.TextRecognitionService.properties.EmailSenderProps;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component @RequiredArgsConstructor
public class EmailSenderConfiguration {
private final EmailSenderProps emailSenderProps;
    @Bean
    @PostConstruct
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(emailSenderProps.getHost()); // Укажите хост SMTP-сервера
        mailSender.setPort(emailSenderProps.getPort()); // Укажите порт
        mailSender.setUsername(emailSenderProps.getUsername()); // Укажите свой email
        mailSender.setPassword(emailSenderProps.getPassword()); // Укажите пароль от почты

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true"); // Устанавливает режим отладки, чтобы увидеть подробные журналы

        return mailSender;
    }
}
