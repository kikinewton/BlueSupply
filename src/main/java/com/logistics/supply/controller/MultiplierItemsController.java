package com.logistics.supply.controller;

import com.logistics.supply.dto.MultipleItemDTO;
import com.logistics.supply.dto.ReqItems;
import com.logistics.supply.dto.RequestItemDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.service.AbstractRestService;
import com.logistics.supply.util.CommonHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Stream;

@RestController
@Slf4j
@RequestMapping("/api")
public class MultiplierItemsController extends AbstractRestService {

    Set<String> nonNulls = Set.of("name", "reason", "purpose", "quantity", "employee");
    List<ReqItems> failed = new ArrayList<>();
    List<RequestItem> completed = new ArrayList<>();


    @PostMapping("/multipleRequestItems")
    public ResponseDTO addBulkRequest(@RequestBody MultipleItemDTO multipleItemDTO) {
        var item = multipleItemDTO.getMultipleRequestItem();
        for (ReqItems x: item) {
            String[] nullValues = CommonHelper.getNullPropertyNames(x);
            System.out.println("count: " + Arrays.stream(nullValues).count());
            System.out.println("for " + x.toString());
            Arrays.asList(nullValues).forEach(c -> System.out.println("Null value: " + c));

            Set<String> l = Set.of(nullValues);

            if (Arrays.stream(nullValues).count() > 0) {
                log.info("Null value found");
                failed.add(x);
            }
            else {
                RequestItem result = createRequestItem(x, multipleItemDTO.getEmployee_id());
                if (Objects.nonNull(result)) completed.add(result);
            }
        }
        failed.forEach((x) -> log.info(x.toString()));
        Map<String, Object> data = new HashMap<>();
        data.put("SUCCESS", completed);
        data.put("ERROR", failed);
        return new ResponseDTO(data);
    }

    private RequestItem createRequestItem(ReqItems itemDTO, int employee_id) {
        RequestItem requestItem = new RequestItem();
        requestItem.setReason(itemDTO.getReason());
        requestItem.setName(itemDTO.getName());
        requestItem.setPurpose(itemDTO.getPurpose());
        requestItem.setQuantity(itemDTO.getQuantity());
        Employee employee = employeeService.getById(employee_id);
        requestItem.setEmployee(employee);
        try {
            RequestItem result = requestItemService.create(requestItem);
            return result;
        }
        catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
