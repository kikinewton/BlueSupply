package com.logistics.supply.service;

import com.logistics.supply.dto.StoreDto;
import com.logistics.supply.model.Store;
import com.logistics.supply.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;

    @Cacheable(value = "AllStores")
    public List<Store> findAll() {

        log.info("Get all stores");
        return storeRepository.findAll();
    }

    @CacheEvict(value = "AllStores", allEntries = true)
    public Store save(StoreDto storeDto) {

        log.info("Create store with name: {}", storeDto.getName());
        Store s = new Store(storeDto.getName());
        return storeRepository.save(s);
    }

    @CacheEvict(value = "AllStores", allEntries = true)
    public void update(int storeId, StoreDto storeDto) {
        log.info("Update store id: {}", storeId);
        storeRepository.updateNameById(storeDto.getName(), storeId);
    }
}
