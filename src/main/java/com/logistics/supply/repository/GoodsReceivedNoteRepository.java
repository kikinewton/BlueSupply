package com.logistics.supply.repository;

import com.logistics.supply.model.GoodsReceivedNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoodsReceivedNoteRepository extends JpaRepository<GoodsReceivedNote, Integer> {
}
