package com.cg.repository;

import com.cg.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    Page<Article> findByStatusAndIsDeletedFalse(Article.Status status, Pageable pageable);

    Page<Article> findByCategoryIdAndStatusAndIsDeletedFalse(Long categoryId, Article.Status status, Pageable pageable);

    Page<Article> findByAuthorIdAndIsDeletedFalse(Long authorId, Pageable pageable);

    Page<Article> findByStatus(Article.Status status, Pageable pageable);

    @Query("SELECT a FROM Article a WHERE a.status = 'PUBLISHED' AND a.isDeleted = false " +
            "AND (a.title LIKE %:keyword% OR a.content LIKE %:keyword%)")
    Page<Article> searchPublished(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT a.category.id, COUNT(a) as count FROM Article a " +
            "WHERE a.status = 'PUBLISHED' AND a.isDeleted = false " +
            "GROUP BY a.category.id")
    List<Object[]> countArticlesByCategory();

    @Query("SELECT a FROM Article a WHERE a.status = 'PUBLISHED' AND a.isDeleted = false " +
            "ORDER BY a.views DESC, a.createdAt DESC")
    Page<Article> findHotArticles(Pageable pageable);

    // 修正：添加 @Modifying 注解，使用正确的 @Query
    @Modifying
    @Query("UPDATE Article a SET a.views = a.views + 1 WHERE a.id = :articleId")
    void incrementViews(@Param("articleId") Long articleId);
    
    Page<Article> findByAuthorUsernameAndIsDeletedFalseOrderByCreatedAtDesc(String username, Pageable pageable);

    Page<Article> findByAuthorUsernameOrderByCreatedAtDesc(String username, Pageable pageable);

    Page<Article> findByIsDeletedFalseAndStatus(Article.Status status, Pageable pageable);

    Page<Article> findByIsDeletedFalse(Pageable pageable);

    long countByStatusAndIsDeletedFalse(Article.Status status);

    long countByIsDeletedFalse(); // 修改：移除不需要的参数
}