package com.logistics.supply.controller;

import com.logistics.supply.dto.InventoryDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.Inventory;
import com.logistics.supply.service.AbstractRestService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(value = "/api")
@Slf4j
public class InventoryController extends AbstractRestService {


    @GetMapping(value = "/inventory")
    public ResponseDTO<List<Inventory>> getAll() {
        try {
            List<Inventory> list = inventoryService.getAll();
            return new ResponseDTO<>(HttpStatus.OK.name(), list, "SUCCESS");
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), null, "ERROR");
    }

    @GetMapping(value = "/inventory/{inventoryId}")
    public ResponseDTO<Inventory> getInventoryById(@PathVariable int inventoryId) {
        Inventory inventory = inventoryService.getById(inventoryId).orElse(null);
        try {
            if (Objects.nonNull(inventory)) {
                return new ResponseDTO<>(HttpStatus.OK.name(), inventory, "SUCCESS");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return new ResponseDTO<>("ERROR", null, HttpStatus.NOT_FOUND.name());
    }

    @PostMapping(value = "/inventory")
    public ResponseDTO<Inventory> addInventory(@RequestBody InventoryDTO inventoryDTO) {
        Inventory inventory = new Inventory();
        inventory.setName(inventoryDTO.getName());
        try {
            if(Objects.nonNull(inventory)) {
                Inventory result = inventoryService.add(inventory);
                log.info(result.toString());
                return new ResponseDTO<>("SUCCESS", result, "INVENTORY_ADDED");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return new ResponseDTO<>("ERROR", null, "INVENTORY_NOT_ADDED");
    }

    @DeleteMapping("/inventory/{inventoryId}")
    public ResponseDTO deleteInventory(@PathVariable int inventoryId) {
        try {
            inventoryService.deleteById(inventoryId);
            return new ResponseDTO("SUCCESS",  String.format("INVENTORY WITH ID: %s DELETED", inventoryId));
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return new ResponseDTO<>("ERROR",  "INVENTORY_NOT_DELETED");

    }


}
