package com.ecommerce.sufi.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.sufi.dto.CategoryRequest;
import com.ecommerce.sufi.exception.BadRequestException;
import com.ecommerce.sufi.exception.ResourceNotFoundException;
import com.ecommerce.sufi.model.Category;
import com.ecommerce.sufi.repo.CategoryRepository;
import com.ecommerce.sufi.repo.ProductRepository;

@Service
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public Category createCategory(CategoryRequest request) {
        String name = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Category already exists");
        }

        Category category = new Category();

        category.setName(name);
        category.setDescription(normalizeDescription(request.getDescription()));
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        return categoryRepository.save(category);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getCategoryById(Long id) {

        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, CategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        String name = request.getName().trim();
        if (!category.getName().equalsIgnoreCase(name)
                && categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Category already exists");
        }

        category.setName(name);
        category.setDescription(normalizeDescription(request.getDescription()));
        category.setUpdatedAt(LocalDateTime.now());

        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        if (productRepository.existsByCategoryId(id)) {
            throw new BadRequestException("Category cannot be deleted while products are using it");
        }
        categoryRepository.delete(category);
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) return null;
        return description.trim();
    }
}
