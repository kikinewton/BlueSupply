package com.logistics.supply.service;

import com.logistics.supply.configuration.AsyncConfig;
import com.logistics.supply.dto.GeneratedQuoteDTO;
import com.logistics.supply.model.GeneratedQuote;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.repository.GeneratedQuoteRepository;
import com.logistics.supply.util.FileGenerationUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.io.File;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneratedQuoteService {
  private final GeneratedQuoteRepository generatedQuoteRepository;
  private final FileGenerationUtil fileGenerationUtil;
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

  @SneakyThrows
  @Async(AsyncConfig.TASK_EXECUTOR_SERVICE)
  private CompletableFuture<File> generateQuote(GeneratedQuote gen) {
    Supplier supplier = gen.getSupplier();
    Context context = new Context();
    context.setVariable("supplierName", supplier.getName());
    String productDescription =
        gen.getProductDescription() == null ? "" : gen.getProductDescription();
    context.setVariable("description", productDescription);
    String quoteHtml = fileGenerationUtil.parseThymeleafTemplate(generateQuoteTemplate, context);
    String pdfName = supplier.getName().concat(RandomStringUtils.random(7));
    return fileGenerationUtil.generatePdfFromHtml(quoteHtml, pdfName);
  }

}
