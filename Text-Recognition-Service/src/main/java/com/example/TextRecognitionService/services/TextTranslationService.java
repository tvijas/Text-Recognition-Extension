package com.example.TextRecognitionService.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class TextTranslationService {
    private final GoogleCredentials credentials;
    public String translateText(String text, String targetLanguage) {
        Translate translate = TranslateOptions.newBuilder().setCredentials(credentials).build().getService();

        Detection detection = translate.detect(text);
        String sourceLanguage = detection.getLanguage();

        Translation translation = translate.translate(text, Translate.TranslateOption.sourceLanguage(sourceLanguage),
                Translate.TranslateOption.targetLanguage(targetLanguage));
        return translation.getTranslatedText();
    }
}

