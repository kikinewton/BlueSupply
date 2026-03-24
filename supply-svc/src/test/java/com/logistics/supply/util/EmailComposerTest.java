package com.logistics.supply.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static org.assertj.core.api.Assertions.assertThat;

class EmailComposerTest {

  private EmailComposer emailComposer;

  @BeforeEach
  void setUp() {
    ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
    resolver.setPrefix("templates/");
    resolver.setSuffix(".html");
    resolver.setTemplateMode(TemplateMode.HTML);
    resolver.setCharacterEncoding("UTF-8");

    SpringTemplateEngine engine = new SpringTemplateEngine();
    engine.setTemplateResolver(resolver);

    emailComposer = new EmailComposer(engine);
  }

  @Test
  void buildEmailWithTable_includesTitle() {
    String result = emailComposer.buildEmailWithTable("MY TITLE", "msg", "<tr><td>x</td></tr>");
    assertThat(result).contains("<title>MY TITLE</title>");
  }

  @Test
  void buildEmailWithTable_includesMessage() {
    String result = emailComposer.buildEmailWithTable("t", "Hello World", "<tr><td>x</td></tr>");
    assertThat(result).contains("Hello World");
  }

  @Test
  void buildEmailWithTable_includesTableContent() {
    String tableContent = "<tr><td>Item A</td><td>5</td></tr>";
    String result = emailComposer.buildEmailWithTable("t", "msg", tableContent);
    assertThat(result).contains("Item A");
  }

  @Test
  void buildEmailWithTable_isValidHtml() {
    String result = emailComposer.buildEmailWithTable("t", "msg", "<tr><td>x</td></tr>");
    assertThat(result).startsWith("<!DOCTYPE");
    assertThat(result).contains("<html");
    assertThat(result).contains("</html>");
  }

  @Test
  void buildEmailWithTable_hasEmailStructure() {
    String result = emailComposer.buildEmailWithTable("t", "msg", "<tr><td>x</td></tr>");
    assertThat(result).contains("<head>");
    assertThat(result).contains("<body");
    assertThat(result).contains("styled-table");
  }
}
