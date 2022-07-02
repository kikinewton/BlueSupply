package com.logistics.supply.repository;

import com.logistics.supply.model.GoodsReceivedNoteComment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoodsReceivedNoteCommentRepository
    extends CrudRepository<GoodsReceivedNoteComment, Long> {

  List<GoodsReceivedNoteComment> findByReadFalseAndEmployeeId(int employeeId);
  List<GoodsReceivedNoteComment> findByGoodsReceivedNoteId(long id);


}
