package com.logistics.supply.service;

import com.logistics.supply.model.RequestItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class RequestItemService extends AbstractDataService {


    public List<RequestItem> findAll(int pageNo, int pageSize) {
        Pageable paging = PageRequest.of(pageNo, pageSize);
        List<RequestItem> requestItemList = new ArrayList<>();
        try {
            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdDate"));
            Page<RequestItem> items = requestItemRepository.findAll(pageable);
            items.forEach(requestItemList::add);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return requestItemList;
    }

    public RequestItem create(RequestItem item) {
        try {
            return requestItemRepository.save(item);
        }
        catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public Optional<RequestItem> findById(int requestItemId) {
        Optional<RequestItem> requestItem = null;
        try {
            requestItem = requestItemRepository.findById(requestItemId);
            return requestItem;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<RequestItem> getRequestDateGreaterThan(String date) {
        List<RequestItem> itemList = new ArrayList<>();
        try {
            List<RequestItem> requestItemList = requestItemRepository.getRequestBetweenDateAndNow(date);
            requestItemList.forEach(itemList::add);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return itemList;
    }
}
