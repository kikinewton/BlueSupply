//package com.logistics.supply.email;
//
//import com.logistics.supply.enums.EmailType;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//
//import javax.mail.MessagingException;
//import javax.mail.internet.MimeMessage;
//import java.nio.charset.StandardCharsets;
//
//
//@Service
//@AllArgsConstructor
//@Slf4j
//public class EmailService implements EmailSender{
//
//
//    private JavaMailSender mailSender;
//
//    @Override
//    public void sendMail(String to, EmailType type, String email) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
//            String html = email;
//            switch (type) {
//                case NEW_REQUEST_MAIL:
//                    helper.setTo(to);
//                    helper.setText(html, Boolean.TRUE);
//                    helper.setSubject("NEW REQUEST");
//                    helper.setFrom("NEW_USER_EMAIL_FROM");
//                    break;
//
//                case PROCUREMENT_REVIEW_MAIL:
//                    helper.setTo(to);
//                    helper.setText(html, Boolean.TRUE);
//                    helper.setSubject("PROCUREMENT");
//                    helper.setFrom("HOD");
//                    break;
//
//                case REQUEST_ENDORSEMENT_MAIL:
//                    helper.setTo(to);
//                    helper.setText(html, Boolean.TRUE);
//                    helper.setSubject("PROCUREMENT");
//                    helper.setFrom("");
//                    break;
//                case CANCEL_REQUEST_MAIL:
//                    helper.setTo(to);
//                    helper.setText(html, Boolean.TRUE);
//                    helper.setSubject("CANCEL");
//                    helper.setFrom("");
//                    break;
//
//                case GENERAL_MANAGER_APPROVAL_MAIL:
//                    helper.setTo(to);
//                    helper.setText(html, Boolean.TRUE);
//                    helper.setSubject("AP");
//                    helper.setFrom("GM");
//                    break;
//            }
//
//        }
//        catch (MessagingException e) {
//            log.error(e.getMessage());
//            e.printStackTrace();
//        }
//
//
//    }
//}
