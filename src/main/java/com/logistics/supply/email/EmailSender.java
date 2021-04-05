package com.logistics.supply.email;

import com.logistics.supply.enums.EmailType;

public interface EmailSender {
    void sendMail(String to, EmailType type, String email);
}
