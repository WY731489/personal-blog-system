package com.cg.repository;

import com.cg.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByArticleIdAndIsDeletedFalse(Long articleId, Pageable pageable);

    Page<Comment> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);

    Long countByArticleIdAndIsDeletedFalse(Long articleId);
}