package com.logistics.supply.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;

@Component
@RequiredArgsConstructor
public class EmailSenderUtil {
    private final SpringTemplateEngine templateEngine;
    private final EmailSender emailSender;
    @Value("${config.template.loginUrl:http://172.16.1.6:4000/}")
    private String loginUrl;


    private String composeEmail(String title, String message, String template) {
        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("message", message);
        context.setVariable("loginUrl", loginUrl);
        return templateEngine.process(template, context);
    }

    public void sendComposeAndSendEmail(String title, String message, String template, EmailType emailType, String to) {
        String emailContent = composeEmail(title, message, template);
        emailSender.sendMail(to, emailType, emailContent);
    }
}
