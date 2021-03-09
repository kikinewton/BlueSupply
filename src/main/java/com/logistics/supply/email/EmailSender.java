package com.logistics.supply.email;

import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.Employee;

import javax.mail.MessagingException;

public interface EmailSender {
    void sendMail(String from, String to, EmailType type, String email);
}
