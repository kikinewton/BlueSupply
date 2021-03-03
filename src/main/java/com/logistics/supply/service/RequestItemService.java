package com.logistics.supply.service;

import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.logistics.supply.enums.EndorsementStatus.*;
import static com.logistics.supply.enums.RequestApproval.APPROVED;
import static com.logistics.supply.util.Constants.*;

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

    public String endorseRequest(int requestItemId) {
        Optional<RequestItem> requestItem = findById(requestItemId);
        if (requestItem.isPresent()) {
            requestItem.get().setEndorsement(ENDORSED);
            requestItem.get().setEndorsementDate(new Date());
            RequestItem result = requestItemRepository.save(requestItem.get());
            if (Objects.nonNull(result)) return REQUEST_ENDORSED;
        }
        return REQUEST_ENDORSEMENT_DENIED;
    }

    public String approveRequest(int requestItemId) {
        Optional<RequestItem> requestItem = findById(requestItemId);
        if (requestItem.isPresent() && requestItem.get().getEndorsement().equals(REQUEST_ENDORSED)) {
            requestItem.get().setApproval(APPROVED);
            requestItem.get().setApprovalDate(new Date());
            RequestItem result = requestItemRepository.save(requestItem.get());
            if (Objects.nonNull(result)) return REQUEST_APPROVED;
        }
        return REQUEST_APPROVAL_DENIED;
    }
}
