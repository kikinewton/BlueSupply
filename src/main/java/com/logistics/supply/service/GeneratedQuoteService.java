package com.logistics.supply.service;

import com.logistics.supply.configuration.AsyncConfig;
import com.logistics.supply.dto.GeneratedQuoteDTO;
import com.logistics.supply.model.GeneratedQuote;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.repository.GeneratedQuoteRepository;
import com.lowagie.text.DocumentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneratedQuoteService {

  private final GeneratedQuoteRepository generatedQuoteRepository;
  private final SupplierService supplierService;
  private final SpringTemplateEngine templateEngine;

  @Value("${config.generatedQuote.template}")
  String generateQuoteTemplate;

  @Async(AsyncConfig.TASK_EXECUTOR_SERVICE)
  public CompletableFuture<File> createQuoteForUnregisteredSupplier(GeneratedQuoteDTO quoteDTO) {
      CompletableFuture<File> res = null;
      GeneratedQuote generatedQuote = new GeneratedQuote();
      BeanUtils.copyProperties(quoteDTO, generatedQuote);
      GeneratedQuote result = generatedQuoteRepository.save(generatedQuote);
      if (result != null) {
          res = generateQuote(result);
      }
      return res;
  }

  @Async(AsyncConfig.TASK_EXECUTOR_SERVICE)
  private CompletableFuture<File> generateQuote(GeneratedQuote gen) {
    Supplier supplier = gen.getSupplier();
    Context context = new Context();
    context.setVariable("supplierName", supplier.getName());
    String productDescription =
        gen.getProductDescription() == null ? "" : gen.getProductDescription();
    context.setVariable("description", productDescription);
    String quoteHtml = parseThymeleafTemplate(context);
    String pdfName = supplier.getName().concat(RandomStringUtils.random(7));
    return generatePdfFromHtml(quoteHtml, pdfName);
  }

  public String parseThymeleafTemplate(Context context) {
    return templateEngine.process(generateQuoteTemplate, context);
  }

  @Async(AsyncConfig.TASK_EXECUTOR_SERVICE)
  public CompletableFuture<File> generatePdfFromHtml(String html, String pdfName) {
    return CompletableFuture.supplyAsync(
        () -> {
          File file = null;
          try {
            file = File.createTempFile(pdfName, ".pdf");
          } catch (IOException e) {
            log.error(e.toString());
          }
          OutputStream outputStream = null;
          try {
            outputStream = new FileOutputStream(file);
          } catch (FileNotFoundException e) {
            log.error(e.toString());
          }
          ITextRenderer renderer = new ITextRenderer();
          renderer.setDocumentFromString(html);
          renderer.layout();
          try {
            renderer.createPDF(outputStream);
          } catch (DocumentException e) {
            log.error(e.toString());
          }
          try {
            outputStream.close();
          } catch (IOException e) {
            log.error(e.toString());
          }
          return file;
        });
  }
}
