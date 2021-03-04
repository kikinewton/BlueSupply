package com.logistics.supply.email;

import com.logistics.supply.enums.EmailType;

import javax.mail.MessagingException;

public interface EmailSender {
    void sendMail(String to, EmailType type, String email);
}
