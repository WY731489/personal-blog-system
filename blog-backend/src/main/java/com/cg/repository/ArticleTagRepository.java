package com.cg.repository;


import com.cg.entity.ArticleTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleTagRepository extends JpaRepository<ArticleTag, Long> {
    void deleteByArticleId(Long articleId);

    List<ArticleTag> findByArticleId(Long articleId);
}