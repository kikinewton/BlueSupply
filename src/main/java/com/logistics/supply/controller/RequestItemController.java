package com.logistics.supply.controller;

import com.logistics.supply.dto.RequestItemDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.service.AbstractRestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping(value = "/api")
public class RequestItemController extends AbstractRestService {

    @GetMapping(value = "/requestItems")
    public ResponseDTO<List<RequestItem>> getAll(@RequestParam(defaultValue = "0") int pageNo,
                                                 @RequestParam(defaultValue = "15") int pageSize) {
        try {
            List<RequestItem> itemList = requestItemService.findAll(pageNo, pageSize);
            if (!itemList.isEmpty()) return new ResponseDTO<>("SUCCESS", itemList, "REQUEST_ITEMS_FOUND");
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return new ResponseDTO<>("ERROR", null, "REQUEST_ITEMS_NOT_FOUND");
    }

    @GetMapping(value = "/requestItems/{requestItemId}")
    public ResponseDTO<RequestItem> getById(@PathVariable int requestItemId) {
        try {
            Optional<RequestItem> item = requestItemService.findById(requestItemId);
            if(item.isPresent()) return new ResponseDTO<>("SUCCESS", item.get(), "REQUEST_ITEM_FOUND");
        }
        catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return new ResponseDTO<>("ERROR", null, "REQUEST_ITEM_NOT_FOUND");
    }

    @PostMapping(value = "/requestItems")
    public ResponseDTO<RequestItem> createRequestItem(@RequestBody RequestItemDTO itemDTO) {
        RequestItem requestItem = new RequestItem();
        requestItem.setReason(itemDTO.getReason());
        requestItem.setName(itemDTO.getName());
        requestItem.setPurpose(itemDTO.getPurpose());
        requestItem.setQuantity(itemDTO.getQuantity());
        requestItem.setEmployee(itemDTO.getEmployee());
        try {
            RequestItem result = requestItemService.create(requestItem);
            if(Objects.nonNull(result)) return new ResponseDTO<>("SUCCESS", result, "REQUEST_ITEM_CREATED");
        }
        catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return new ResponseDTO<>("ERROR", null, "REQUEST_ITEM_NOT_CREATED");
    }

}
