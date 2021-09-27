package com.logistics.supply.repository;

import com.logistics.supply.model.GeneratedQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeneratedQuoteRepository extends JpaRepository<GeneratedQuote, Integer> {
}
