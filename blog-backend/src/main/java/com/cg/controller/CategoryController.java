package com.cg.controller;

import com.cg.dto.CategoryDTO;
import com.cg.Service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/categories")  // 修改：去掉重复的 /api
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDTO.Response>> getAllCategories() {
        List<CategoryDTO.Response> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDTO.Response> createCategory(
            @Valid @RequestBody CategoryDTO.CreateRequest request) {
        CategoryDTO.Response category = categoryService.createCategory(request);
        return ResponseEntity.ok(category);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDTO.Response> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO.UpdateRequest request) {
        CategoryDTO.Response category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok("删除成功");
    }
}