package com.example.TextRecognitionService.services;
import com.example.TextRecognitionService.models.template.MailTemplate;
import com.example.TextRecognitionService.properties.EmailSenderProps;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class EmailSenderService {
    private final EmailSenderProps emailSenderProps;
    private final JavaMailSender javaMailSender;
    public void sendEmail(MailTemplate mailTemplate) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(emailSenderProps.getUsername());
            message.setTo(mailTemplate.getToEmail());
            message.setSubject(mailTemplate.getSubject());
            message.setText(mailTemplate.getBody());

            javaMailSender.send(message);

            System.out.println("Email sent successfully");
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

}
