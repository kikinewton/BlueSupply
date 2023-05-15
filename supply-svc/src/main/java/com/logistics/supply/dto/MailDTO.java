package com.logistics.supply.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MailDTO {

    private String mailFrom;

    private String emailType;

    private String name;

    private String mailTo;

    private String mailCc;

    private String mailBcc;

    private String mailSubject;

    private String mailContent;

    private String contentType;

    private List< Object > attachments;

    private Map< String, Object > model;

    public MailDTO() {
        this.contentType = "text/plain";
    }

    public MailDTO(String from, String to, String subject, String content) {
        this.mailFrom = from;
        this.mailTo = to;
        this.mailSubject = subject;
        this.mailContent = content;
    }

}
