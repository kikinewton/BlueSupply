package com.logistics.supply.service;

import com.logistics.supply.dto.GeneratedQuoteDTO;
import com.logistics.supply.model.GeneratedQuote;
import com.logistics.supply.repository.GeneratedQuoteRepository;
import com.lowagie.text.DocumentException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GeneratedQuoteService {

  private final GeneratedQuoteRepository generatedQuoteRepository;
  private final SupplierService supplierService;
  private final SpringTemplateEngine templateEngine;


  @Value("${config.generatedQuote.template}")
  String generateQuoteTemplate;

  public File createQuoteForUnregisteredSupplier(GeneratedQuoteDTO quoteDTO) throws Exception {
    GeneratedQuote generatedQuote = new GeneratedQuote();
    BeanUtils.copyProperties(quoteDTO, generatedQuote);
    GeneratedQuote result = generatedQuoteRepository.save(generatedQuote);
    if (Objects.isNull(result)) return null;
    return generateQuote(result.getId());
  }

  private File generateQuote(int id) throws Exception {
    GeneratedQuote gen = generatedQuoteRepository.findById(id).orElseThrow(Exception::new);
    String supplier = supplierService.findById(gen.getSupplierName().getId()).getName();
    Context context = new Context();
    context.setVariable("supplierName", supplier);
    context.setVariable("phoneNo", gen.getPhoneNo());
    context.setVariable("location", gen.getLocation());
    context.setVariable("deliveryDate", gen.getDeliveryDate());
    context.setVariable("description", gen.getProductDescription());
    String quoteHtml = parseThymeleafTemplate(context);
    String pdfName = supplier.concat(RandomStringUtils.random(7));
    return generatePdfFromHtml(quoteHtml, pdfName);
  }

  public String parseThymeleafTemplate(Context context) {
    return templateEngine.process(generateQuoteTemplate, context);
  }

  public File generatePdfFromHtml(String html, String pdfName)
      throws IOException, DocumentException {
    File file = File.createTempFile(pdfName, ".pdf");

    OutputStream outputStream = new FileOutputStream(file);
    ITextRenderer renderer = new ITextRenderer();
    renderer.setDocumentFromString(html);
    renderer.layout();
    renderer.createPDF(outputStream);
    outputStream.close();
    if (Objects.isNull(file)) return null;
    return file;
  }
}
