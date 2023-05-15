package com.logistics.supply.service;

import com.logistics.supply.model.PaymentReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.repository.PaymentReportRepository;
import java.util.Date;

import static com.logistics.supply.util.Constants.NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReportService {
    private final PaymentReportRepository paymentReportRepository;

    public Page<PaymentReport> findBetweenDate(int pageNo, int pageSize, Date startDate, Date endDate) throws GeneralException {
        try {
            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
            return paymentReportRepository.findByPaymentDateBetween(startDate, endDate, pageable);
        } catch (Exception e) {
            log.error(e.toString());
        }
        throw new GeneralException(NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    public Page<PaymentReport> findBySupplier(
            int pageNo, int pageSize, Date startDate, Date endDate, String supplier) throws GeneralException {
        try {
            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
            return paymentReportRepository.findByDateReceivedBetweenAndSupplierIgnoreCase(
                    startDate, endDate, supplier, pageable);
        } catch (Exception e) {
            log.error(e.toString());
        }
        throw new GeneralException(NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
