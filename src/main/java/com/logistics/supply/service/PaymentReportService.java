package com.logistics.supply.service;

import com.logistics.supply.model.PaymentReport;
import com.logistics.supply.repository.PaymentReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReportService {
    private final PaymentReportRepository paymentReportRepository;

    public Page<PaymentReport> findBetweenDate(int pageNo, int pageSize, Date startDate, Date endDate) {
        try {
            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
            return paymentReportRepository.findByDateReceivedBetween(startDate, endDate, pageable);
        } catch (Exception e) {
            log.error(e.toString());
        }
        return null;
    }

    public Page<PaymentReport> findBySupplier(
            int pageNo, int pageSize, Date startDate, Date endDate, String supplier) {
        try {
            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
            return paymentReportRepository.findByDateReceivedBetweenAndSupplierIgnoreCase(
                    startDate, endDate, supplier, pageable);
        } catch (Exception e) {
            log.error(e.toString());
        }
        return null;
    }
}
