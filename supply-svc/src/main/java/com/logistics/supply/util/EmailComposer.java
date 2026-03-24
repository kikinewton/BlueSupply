package com.logistics.supply.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
@RequiredArgsConstructor
public class EmailComposer {

  private final SpringTemplateEngine templateEngine;

  public String buildEmailWithTable(String title, String message, String table) {
    Context context = new Context();
    context.setVariable("title", title);
    context.setVariable("message", message);
    context.setVariable("table", table);
    return templateEngine.process("email-with-table", context);
  }
}
