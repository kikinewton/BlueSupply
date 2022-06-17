package com.logistics.supply.util;

import com.logistics.supply.configuration.AsyncConfig;
import com.lowagie.text.DocumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class FileGenerationUtil {
   private final SpringTemplateEngine templateEngine;

  @Async(AsyncConfig.TASK_EXECUTOR_SERVICE)
  public CompletableFuture<File> generatePdfFromHtml(String html, String pdfName)
      throws IOException, DocumentException {
    return CompletableFuture.supplyAsync(() -> {

      File file = null;
      try {
        file = File.createTempFile(pdfName, ".pdf");
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      OutputStream outputStream = null;
      try {
        outputStream = new FileOutputStream(file);
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }
      ITextRenderer renderer = new ITextRenderer();
      renderer.setDocumentFromString(html);
      renderer.layout();
      try {
        renderer.createPDF(outputStream);
      } catch (DocumentException e) {
        throw new RuntimeException(e);
      }
      try {
        outputStream.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return file;
    });
  }

  public String parseThymeleafTemplate(String template, Context context) {
    return templateEngine.process(template, context);
  }
}
