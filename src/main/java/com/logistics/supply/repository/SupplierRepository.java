package com.logistics.supply.repository;

import com.logistics.supply.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer> {

  Optional<Supplier> findByName(String name);

  @Query(
      value =
          "SELECT * from supplier s where s.id in "
              + "( SELECT ris.supplier_id from request_item_suppliers ris join "
              + "request_item ri on ris.request_id = ri.id and ri.endorsement = 'ENDORSED')",
      nativeQuery = true)
  List<Supplier> findSuppliersWithNonFinalRequestProcurement();
}
