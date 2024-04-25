package com.example.TextRecognitionService.services;


import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;

@Service
public class TextRecognitionService {
    private final ImageAnnotatorClient imageAnnotatorClient;

    @Autowired
    public TextRecognitionService(ImageAnnotatorClient imageAnnotatorClient) {
        this.imageAnnotatorClient = imageAnnotatorClient;
    }

    public String detectText(String base64Data) {
        try {
            // Преобразование BufferedImage в ByteString
            byte[] decodedBytes = Base64.getDecoder().decode(base64Data);

            // Создаем объект ByteString
            ByteString imgBytes = ByteString.copyFrom(decodedBytes);
            // Создание запроса для обнаружения текста
            Image img = Image.newBuilder().setContent(imgBytes).build();
            Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();

            // Выполнение запроса
            BatchAnnotateImagesResponse response = imageAnnotatorClient.batchAnnotateImages(List.of(request));

            // Обработка результатов
            List<AnnotateImageResponse> responses = response.getResponsesList();
            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    return null;
                }
                // Обработка результатов обнаружения текста
                for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
                    return annotation.getDescription();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
}
