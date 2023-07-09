package com.logistics.supply.repository;

import com.logistics.supply.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Integer> {
    @Transactional
    @Modifying
    @Query("update Store s set s.name = ?1 where s.id = ?2")
    void updateNameById(String name, Integer id);
}
