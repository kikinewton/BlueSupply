package com.logistics.supply;

import com.logistics.supply.configuration.FileStorageProperties;
import com.logistics.supply.model.Employee;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.repository.RequestItemRepository;
import com.logistics.supply.util.EmailComposer;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.*;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.*;
// import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableAsync
@EnableCaching(proxyTargetClass = true)
@EnableScheduling
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableConfigurationProperties({FileStorageProperties.class})
public class SupplyApplication {

    @Autowired
    private RequestItemRepository requestItemRepository;


  public static void main(String[] args) {
    SpringApplication.run(SupplyApplication.class, args);
//      List<String> title = new ArrayList<>();
//      title.add("Name");
//      title.add("Phone No");
//      title.add("Email");
//
//      SupplierDto sup1 = new SupplierDto("Abc Packaging Limited", "00393939302", "abcpackagingltd@gmail.com");
//      SupplierDto sup2 = new SupplierDto("Kong Glass Manufacturing", "002939111393", "kongEnt34@hotmail.com");
//      SupplierDto sup3 = new SupplierDto("Long Rich Chemicals", "020399190009", "longrichchemicals@yahoomail.com");
//      SupplierDto sup4 = new SupplierDto("Abc Packaging Limited", "00393939302", "abcpackagingltd@gmail.com");
//      SupplierDto sup5 = new SupplierDto("Abc Packaging Limited", "00393939302", "abcpackagingltd@gmail.com");
//      SupplierDto sup6 = new SupplierDto("Abc Packaging Limited", "00393939302", "abcpackagingltd@gmail.com");
//
//      List<SupplierDto> sups = new ArrayList<>();
//      sups.add(sup1);
//      sups.add(sup2);
//      sups.add(sup3);
//      sups.add(sup4);
//      sups.add(sup5);
//      sups.add(sup6);
//
//      String table = buildHtmlTable(title, sups);
//
//      String htmlLink = EmailComposer.buildEmailWithTable(table);
//      System.out.println(htmlLink);



  }




    private static String buildHtmlTable(List<String> title, List<SupplierDto> suppliers) {
        StringBuilder header = new StringBuilder();
        for (String t : title) header.append(String.format(tableHeader, t));

        header = new StringBuilder(String.format(tableRow, header.toString()));
        String sb =
                suppliers.stream()
                        .map(
                                s ->
                                        String.format(tableData, s.getName())
                                                + String.format(tableData, s.getPhone_no())
                                                + String.format(tableData, s.getEmail()))
                        .map(t -> String.format(tableRow, t))
                        .collect(Collectors.joining("", "", ""));

        return header.toString().concat(sb);
    }
}

 class SupplierDto {
    private String name;
    private String phone_no;
    private String email;

    public SupplierDto(String name, String phone_no, String email) {
        this.name = name;
        this.phone_no = phone_no;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getPhone_no() {
        return phone_no;
    }

    public String getEmail() {
        return email;
    }
}

