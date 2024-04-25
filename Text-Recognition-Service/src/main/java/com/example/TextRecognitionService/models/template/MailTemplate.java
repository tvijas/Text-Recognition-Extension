package com.example.TextRecognitionService.models.template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MailTemplate {
    private String toEmail;
    private String subject;
    private String body;

    public MailTemplate setStandardEmailTemplate(String toEmail, int vcode){
        this.toEmail = toEmail;
        this.subject = "Email confirmation!!!";
        this.body =  "IT is your code: "+ vcode;
        return new MailTemplate();
    }

}
