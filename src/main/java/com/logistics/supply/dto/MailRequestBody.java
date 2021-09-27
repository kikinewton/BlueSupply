package com.logistics.supply.dto;

import com.logistics.supply.enums.EmailType;
import lombok.Data;

@Data
public class MailRequestBody {

    private String to;
    private String emailContent;
    private EmailType emailType;
}
