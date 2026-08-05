package com.ecommerce.sufi.services;

import java.util.List;

import com.ecommerce.sufi.dto.CategoryRequest;
import com.ecommerce.sufi.model.Category;

public interface CategoryService {

    Category createCategory(CategoryRequest request);

    List<Category> getAllCategories();

    Category getCategoryById(Long id);

    Category updateCategory(Long id, CategoryRequest request);

    void deleteCategory(Long id);
}