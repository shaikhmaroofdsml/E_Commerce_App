package com.ecommerce.product.service;

import com.ecommerce.product.dto.CategoryRequest;
import com.ecommerce.product.dto.CategoryResponse;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findByActiveTrue()
            .stream().map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                .orElseThrow(() -> new ProductNotFoundException("Parent category not found"));
        }

        String slug = request.getSlug() != null
            ? request.getSlug()
            : request.getName().toLowerCase().replaceAll("[^a-z0-9]", "-");

        Category category = Category.builder()
            .name(request.getName())
            .slug(slug)
            .parent(parent)
            .build();

        return mapToResponse(categoryRepository.save(category));
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Category not found: " + id));
        category.setActive(false);
        categoryRepository.save(category);
    }

    private CategoryResponse mapToResponse(Category c) {
        return CategoryResponse.builder()
            .id(c.getId())
            .name(c.getName())
            .slug(c.getSlug())
            .parentId(c.getParent() != null ? c.getParent().getId() : null)
            .parentName(c.getParent() != null ? c.getParent().getName() : null)
            .active(c.isActive())
            .build();
    }
}
