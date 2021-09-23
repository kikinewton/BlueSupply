//package com.logistics.supply.service;
//
//import com.logistics.supply.model.Comment;
//import com.logistics.supply.repository.CommentRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import javax.transaction.Transactional;
//import java.io.Serializable;
//import java.util.List;
//import java.util.Optional;
//
//@Service
//@Transactional
//public abstract class CommentRepositoryImpl<T extends Comment, ID extends Serializable>
//        implements CommentService<T, ID> {
//
//    private CommentRepository<T, ID> commentRepository;
//
//    @Autowired
//    public CommentRepositoryImpl(CommentRepository<T, ID> commentRepository) {
//        this.commentRepository = commentRepository;
//    }
//
//    @Override
//    public T save(T entity) {
//        return (T) commentRepository.save(entity);
//    }
//
//    @Override
//    public List<T> findAll() {
//        return commentRepository.findAll();
//    }
//
//    @Override
//    public Optional<T> findById(ID entityId) {
//        return commentRepository.findById(entityId);
//    }
//
//    @Override
//    public T update(T entity) {
//        return (T) commentRepository.save(entity);
//    }
//
//    @Override
//    public T updateById(T entity, ID entityId) {
//        Optional<T> optional = commentRepository.findById(entityId);
//        if(optional.isPresent()){
//            return (T) commentRepository.save(entity);
//        }else{
//            return null;
//        }
//    }
//
////    @Override
////    public void delete(T entity) {
////        commentRepository.delete(entity);
////    }
//
//
//
//
//}
