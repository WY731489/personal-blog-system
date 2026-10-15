package com.cg.controller;

import com.cg.Service.CommentService;
import com.cg.dto.CommentDTO;
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
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/article/{articleId}")
    public ResponseEntity<Page<CommentDTO.Response>> getCommentsByArticleId(
            @PathVariable Long articleId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(commentService.getCommentsByArticleId(articleId, pageable));
    }

    @PostMapping("/article/{articleId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDTO.Response> createComment(
            @PathVariable Long articleId,
            @Valid @RequestBody CommentDTO.CreateRequest request) {
        return ResponseEntity.ok(commentService.createComment(articleId, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}