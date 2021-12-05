package com.logistics.supply.event.listener;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.model.Floats;
import com.logistics.supply.repository.FloatGRNRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.util.Set;

import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Component
@RequiredArgsConstructor
public class FloatGRNListener {

    private final FloatGRNRepository floatGRNRepository;
    private final EmailSender emailSender;

    @Value("${config.templateMail}")
    String newCommentEmail;

    private SpringTemplateEngine templateEngine;

    public void sendMailToHod(FloatGRNEvent floatGRNEvent) {
        var departmentFloats = floatGRNEvent.getFloats().stream().collect(groupingBy(x -> x.getDepartment()));
//        EmailComposer.buildEmailWithTable()
    }



    @Getter
    public static class FloatGRNEvent extends ApplicationEvent {

        private Set<Floats> floats;

        public FloatGRNEvent(Object source, Set<Floats> floats) {
            super(source);
            this.floats = floats;
        }
    }
}
