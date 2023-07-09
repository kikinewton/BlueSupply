package com.logistics.supply.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.logistics.supply.model.GoodsReceivedNoteComment;
import java.util.List;

@Repository
public interface GoodsReceivedNoteCommentRepository
    extends CrudRepository<GoodsReceivedNoteComment, Long> {

  List<GoodsReceivedNoteComment> findByGoodsReceivedNoteId(long id);


}
