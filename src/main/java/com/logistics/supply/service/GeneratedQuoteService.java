package com.logistics.supply.service;

import com.logistics.supply.dto.GeneratedQuoteDTO;
import com.logistics.supply.dto.ItemUpdateDTO;
import com.logistics.supply.errorhandling.GeneralException;
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
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneratedQuoteService {
  private final GeneratedQuoteRepository generatedQuoteRepository;
  private final FileGenerationUtil fileGenerationUtil;

  @Value("${config.generatedQuote.template}")
  String generateQuoteTemplate;

  public File createQuoteForUnregisteredSupplier(GeneratedQuoteDTO quoteDTO)
      throws GeneralException {
    GeneratedQuote generatedQuote = new GeneratedQuote();
    BeanUtils.copyProperties(quoteDTO, generatedQuote);
    getProductFromList(quoteDTO, generatedQuote);
    generatedQuoteRepository.save(generatedQuote);
    return generateQuote(quoteDTO);
  }

  private void getProductFromList(GeneratedQuoteDTO quoteDTO, GeneratedQuote generatedQuote) {
    if (quoteDTO.getItems() != null && !quoteDTO.getItems().isEmpty()) {
      String product = composeProductDescription(quoteDTO.getItems());
      generatedQuote.setProductDescription(product);
    }
  }

  private String composeProductDescription(List<ItemUpdateDTO> items) {
    StringBuilder product = new StringBuilder();
    items.forEach(
        i -> {
          String s = i.toString();
          String s1 = s.replace("ItemUpdateDTO(", "");
          String s2 = s1.replace(")", "");
          product.append(s2);
        });
    return product.toString();
  }

  @SneakyThrows
  private File generateQuote(GeneratedQuoteDTO gen) {
    Supplier supplier = gen.getSupplier();
    Context context = new Context();
    context.setVariable("supplierName", supplier.getName());
    String productDescription =
        gen.getProductDescription() == null ? "" : gen.getProductDescription();
    List<ItemUpdateDTO> items = gen.getItems();
    List<String> productList =
        Arrays.stream(productDescription.split(",")).collect(Collectors.toList());
    context.setVariable("description", items);
    BigDecimal totalCost =
        items.stream()
            .map(i -> i.getEstimatedPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    context.setVariable("totalCost", totalCost);
    String quoteHtml = fileGenerationUtil.parseThymeleafTemplate(generateQuoteTemplate, context);
    String pdfName = supplier.getName().concat(RandomStringUtils.random(7));
    return fileGenerationUtil.generatePdfFromHtml(quoteHtml, pdfName).join();
  }
}
