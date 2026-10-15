package com.cg.Service;

import com.cg.dto.CategoryDTO;
import com.cg.entity.Category;
import com.cg.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryDTO.Response> getAllCategories() {
        return categoryRepository.findByStatusTrue().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDTO.Response createCategory(CategoryDTO.CreateRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("分类名称已存在");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setStatus(true);

        categoryRepository.save(category);

        return convertToResponse(category);
    }

    @Transactional
    public CategoryDTO.Response updateCategory(Long id, CategoryDTO.UpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));

        // 检查名称是否重复
        if (!category.getName().equals(request.getName()) &&
                categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("分类名称已存在");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        if (request.getStatus() != null) {
            category.setStatus(request.getStatus());
        }

        categoryRepository.save(category);

        return convertToResponse(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));

        category.setStatus(false);
        categoryRepository.save(category);
    }

    private CategoryDTO.Response convertToResponse(Category category) {
        CategoryDTO.Response response = new CategoryDTO.Response();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setStatus(category.getStatus());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        return response;
    }
}