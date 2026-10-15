package com.cg.controller;

import com.cg.dto.ArticleDTO;
import com.cg.Service.ArticleService;
import com.cg.entity.Article; // 添加此行导入Article类
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/articles")  // 修改：去掉重复的 /api
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public ResponseEntity<Page<ArticleDTO.SimpleResponse>> getPublishedArticles(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,  // 显式处理page参数
            @RequestParam(defaultValue = "10") int size,  // 显式处理size参数
            @RequestParam(defaultValue = "publishedAt,desc") String sort) {  // 显式处理sort参数
        
        // 解析排序参数
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        String sortField = sortParams[0];
        
        // 创建Pageable对象
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        
        Page<ArticleDTO.SimpleResponse> articles = articleService.getPublishedArticles(categoryId, pageable);
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleDTO.Response> getArticle(@PathVariable Long id) {
        ArticleDTO.Response article = articleService.getArticle(id);
        return ResponseEntity.ok(article);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ArticleDTO.Response> createArticle(
            @Valid @RequestBody ArticleDTO.CreateRequest request) {
        ArticleDTO.Response article = articleService.createArticle(request);
        return ResponseEntity.ok(article);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ArticleDTO.Response> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleDTO.UpdateRequest request) {
        ArticleDTO.Response article = articleService.updateArticle(id, request);
        return ResponseEntity.ok(article);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<ArticleDTO.SimpleResponse>> getMyArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        String sortField = sortParams[0];
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        
        Page<ArticleDTO.SimpleResponse> articles = articleService.getMyArticles(pageable);
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ArticleDTO.Response>> getAllArticles(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        String sortField = sortParams[0];
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        
        Article.Status articleStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                articleStatus = Article.Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
            }
        }
        
        Page<ArticleDTO.Response> articles = articleService.getAllArticles(articleStatus, pageable);
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ArticleDTO.Response>> getPendingArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        String sortField = sortParams[0];
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        
        Page<ArticleDTO.Response> articles = articleService.getPendingArticles(pageable);
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArticleDTO.StatisticsResponse> getStatistics() {
        ArticleDTO.StatisticsResponse statistics = articleService.getStatistics();
        return ResponseEntity.ok(statistics);
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArticleDTO.Response> reviewArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleDTO.ReviewRequest request) {
        ArticleDTO.Response article = articleService.reviewArticle(id, request);
        return ResponseEntity.ok(article);
    }
}