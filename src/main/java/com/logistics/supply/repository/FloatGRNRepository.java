package com.logistics.supply.repository;

import com.logistics.supply.model.FloatGRN;
import com.logistics.supply.model.Floats;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FloatGRNRepository
    extends CrudRepository<FloatGRN, Long>, JpaSpecificationExecutor<FloatGRN> {
  Optional<FloatGRN> findByFloats(Floats floats);
  @Query(value = "SELECT * FROM float_grn f WHERE f.approved_by_store_manager = true AND f.float_order_id IN (SELECT id FROM float_order fo WHERE fo.retired = false)", nativeQuery = true)
  List<FloatGRN> findByApprovedByStoreManagerIsTrueAndNotRetired();


}
