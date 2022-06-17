package com.logistics.supply.repository;

import com.logistics.supply.model.QuotationComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuotationCommentRepository extends JpaRepository<QuotationComment, Long> {
    List<QuotationComment> findByReadFalseAndEmployeeId(int employeeId);

    List<QuotationComment> findByQuotationId(int id);

}
