package com.cg.controller;

import com.cg.dto.ArticleDTO;
import com.cg.entity.Article;
import com.cg.Service.ArticleService;
import com.cg.Service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ArticleService articleService;
    private final CategoryService categoryService;

    @GetMapping("/articles/pending")
    public ResponseEntity<Page<ArticleDTO.Response>> getPendingArticles(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ArticleDTO.Response> articles = articleService.getArticlesByStatus(Article.Status.PENDING, pageable);
        return ResponseEntity.ok(articles);
    }

    @PutMapping("/articles/{id}/review")
    public ResponseEntity<ArticleDTO.Response> reviewArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleDTO.ReviewRequest request) {
        ArticleDTO.Response article = articleService.reviewArticle(id, request);
        return ResponseEntity.ok(article);
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        // 实现统计数据查询
        return ResponseEntity.ok("统计功能待实现");
    }
}