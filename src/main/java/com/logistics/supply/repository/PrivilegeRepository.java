package com.logistics.supply.repository;

import com.logistics.supply.model.Privilege;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrivilegeRepository extends JpaRepository<Privilege, Integer> {
    @Cacheable(value = "privileges", key = "#name")
    Privilege findByName(String name);
}
