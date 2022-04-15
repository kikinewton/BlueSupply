package com.logistics.supply.service;

import com.logistics.supply.configuration.AsyncConfig;
import com.logistics.supply.dto.GeneratedQuoteDTO;
import com.logistics.supply.model.GeneratedQuote;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.repository.GeneratedQuoteRepository;
import com.lowagie.text.DocumentException;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class GeneratedQuoteService {

  private final GeneratedQuoteRepository generatedQuoteRepository;
  private final SupplierService supplierService;
  private final SpringTemplateEngine templateEngine;

  @Value("${config.generatedQuote.template}")
  String generateQuoteTemplate;

  @Async(AsyncConfig.TASK_EXECUTOR_SERVICE)
  public CompletableFuture<File> createQuoteForUnregisteredSupplier(GeneratedQuoteDTO quoteDTO) throws Exception {
    GeneratedQuote generatedQuote = new GeneratedQuote();
    BeanUtils.copyProperties(quoteDTO, generatedQuote);
    GeneratedQuote result = generatedQuoteRepository.save(generatedQuote);
    return generateQuote(result.getId());
  }

  @Async(AsyncConfig.TASK_EXECUTOR_SERVICE)
  private CompletableFuture<File> generateQuote(int id) throws Exception {
    GeneratedQuote gen = generatedQuoteRepository.findById(id).orElseThrow(Exception::new);
    Supplier supplier = supplierService.findById(gen.getSupplier().getId());
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
  public CompletableFuture<File> generatePdfFromHtml(String html, String pdfName)
      throws IOException, DocumentException {
    return CompletableFuture.supplyAsync(
        () -> {
          File file = null;
          try {
            file = File.createTempFile(pdfName, ".pdf");
          } catch (IOException e) {
            e.printStackTrace();
          }

          OutputStream outputStream = null;
          try {
            outputStream = new FileOutputStream(file);
          } catch (FileNotFoundException e) {
            e.printStackTrace();
          }
          ITextRenderer renderer = new ITextRenderer();
          renderer.setDocumentFromString(html);
          renderer.layout();
          try {
            renderer.createPDF(outputStream);
          } catch (DocumentException e) {
            e.printStackTrace();
          }
          try {
            outputStream.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
          return file;
        });
  }
}
