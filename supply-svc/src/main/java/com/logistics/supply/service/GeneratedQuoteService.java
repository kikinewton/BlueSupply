package com.logistics.supply.service;

import com.logistics.supply.dto.GeneratedQuoteDto;
import com.logistics.supply.dto.ItemUpdateDto;
import com.logistics.supply.exception.SupplierNotFoundException;
import com.logistics.supply.model.GeneratedQuote;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.repository.GeneratedQuoteRepository;
import com.logistics.supply.repository.SupplierRepository;
import com.logistics.supply.util.FileGenerationUtil;
import lombok.RequiredArgsConstructor;
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
    private final SupplierRepository supplierRepository;

    @Value("${config.generatedQuote.template}")
    String generateQuoteTemplate;

    public File createQuoteForUnregisteredSupplier(GeneratedQuoteDto quoteDTO) {
        GeneratedQuote generatedQuote = new GeneratedQuote();
        BeanUtils.copyProperties(quoteDTO, generatedQuote);
        getProductFromList(quoteDTO, generatedQuote);
        generatedQuoteRepository.save(generatedQuote);
        return generateQuote(quoteDTO);
    }

    private void getProductFromList(GeneratedQuoteDto quoteDTO, GeneratedQuote generatedQuote) {
        if (quoteDTO.getItems() != null && !quoteDTO.getItems().isEmpty()) {
            String product = composeProductDescription(quoteDTO.getItems());
            generatedQuote.setProductDescription(product);
        }
    }

    protected String composeProductDescription(List<ItemUpdateDto> items) {

        StringBuilder product = new StringBuilder();

        for (ItemUpdateDto item : items) {

            String itemString = item.toString();

            // Remove leading and trailing parentheses
            itemString = itemString.substring(13, itemString.length() - 1);

            // Append the transformed string to the StringBuilder
            product.append(itemString);

            // Append a new line character to separate items
            product.append('\n');
        }

        // Remove the trailing newline character if there are items
        if (!items.isEmpty()) {
            product.deleteCharAt(product.length() - 1);
        }

        return product.toString();
    }

    private File generateQuote(GeneratedQuoteDto gen) {

        Supplier supplier = supplierRepository.findByNameEqualsIgnoreCase(gen.getSupplier().getName())
                .orElseThrow(() -> new SupplierNotFoundException(gen.getSupplier().getName()));
        Context context = new Context();
        context.setVariable("supplierName", supplier.getName());
        String productDescription =
                gen.getProductDescription() == null ? "" : gen.getProductDescription();
        List<ItemUpdateDto> items = gen.getItems();
        List<String> productList =
                Arrays.stream(productDescription.split(",")).collect(Collectors.toList());
        context.setVariable("description", items);
        context.setVariable("phoneNo", supplier.getPhoneNo() != null ? supplier.getPhoneNo() : "");
        context.setVariable("location", supplier.getLocation() != null ? supplier.getLocation() : "");
        context.setVariable("deliveryDate", gen.getDeliveryDate() != null ? gen.getDeliveryDate() : "");
        BigDecimal totalCost =
                items.stream()
                        .map(i -> i.getEstimatedPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        context.setVariable("totalCost", totalCost);
        String quoteHtml = fileGenerationUtil.parseThymeleafTemplate(generateQuoteTemplate, context);
        String pdfName = supplier.getName() + "_" + System.currentTimeMillis();
        return fileGenerationUtil.generatePdfFromHtml(quoteHtml, pdfName).join();
    }
}
