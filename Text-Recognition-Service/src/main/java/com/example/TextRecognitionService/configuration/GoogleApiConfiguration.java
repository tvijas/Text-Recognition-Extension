package com.example.TextRecognitionService.configuration;

import com.example.TextRecognitionService.Application;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
public class GoogleApiConfiguration {
    @Bean
    @SneakyThrows
    public ImageAnnotatorClient imageAnnotatorClient() {
        return ImageAnnotatorClient.create(
                ImageAnnotatorSettings.newBuilder()
                        .setCredentialsProvider(() -> ServiceAccountCredentials.fromStream(getGoogleCredentialsAsStream()))
                        .build());
    }
    @Bean
    @SneakyThrows
    public GoogleCredentials googleCredentials() {
        return GoogleCredentials.fromStream(getGoogleCredentialsAsStream());
    }

    @SneakyThrows
    public InputStream getGoogleCredentialsAsStream() {
        InputStream inputStream = Application.class.getResourceAsStream("/credentials.json");
        if (inputStream == null)
            throw new Exception("Unable to find credentials.json file in the classpath");
        return inputStream;
    }
}
