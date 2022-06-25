package com.logistics.supply.util;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

@Component
@RequiredArgsConstructor
public class EmailSenderUtil {
    private final SpringTemplateEngine templateEngine;
    private final EmailSender emailSender;


    private String composeEmail(String title, String message, String template) {
        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("message", message);
        return templateEngine.process(template, context);
    }

    public void sendComposeAndSendEmail(String title, String message, String template, EmailType emailType, String to) {
        String emailContent = composeEmail(title, message, template);
        emailSender.sendMail(to, emailType, emailContent);
    }
}
