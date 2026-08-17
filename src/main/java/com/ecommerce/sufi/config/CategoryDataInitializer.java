package com.ecommerce.sufi.config;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.ecommerce.sufi.model.Category;
import com.ecommerce.sufi.repo.CategoryRepository;

@Component
public class CategoryDataInitializer implements ApplicationRunner {

    private static final List<CategorySeed> DEFAULT_CATEGORIES = List.of(
            new CategorySeed("Electronics",
                    "Smartphones, laptops, headphones, smartwatches, cameras, and electronic accessories."),
            new CategorySeed("Fashion",
                    "Men’s and women’s clothing, footwear, watches, bags, and fashion accessories."),
            new CategorySeed("Home & Kitchen",
                    "Furniture, kitchen appliances, cookware, home décor, and household essentials."),
            new CategorySeed("Beauty & Personal Care",
                    "Skincare, haircare, makeup, grooming products, and personal-care essentials."),
            new CategorySeed("Books",
                    "Educational books, novels, competitive-exam guides, comics, and children’s books."),
            new CategorySeed("Sports & Fitness",
                    "Sports equipment, gym accessories, fitness trackers, and activewear."),
            new CategorySeed("Grocery",
                    "Packaged food, beverages, snacks, cooking essentials, and everyday groceries."),
            new CategorySeed("Toys & Games",
                    "Toys, puzzles, board games, learning products, and gaming accessories."),
            new CategorySeed("Automotive",
                    "Car and bike accessories, cleaning products, spare parts, and vehicle-care items."),
            new CategorySeed("Health & Wellness",
                    "Healthcare devices, supplements, wellness products, and daily health essentials."));

    private final CategoryRepository categoryRepository;

    public CategoryDataInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        LocalDateTime now = LocalDateTime.now();

        for (CategorySeed seed : DEFAULT_CATEGORIES) {
            categoryRepository.findByNameIgnoreCase(seed.name())
                    .ifPresentOrElse(category -> addMissingDescription(category, seed.description(), now),
                            () -> createCategory(seed, now));
        }
    }

    private void createCategory(CategorySeed seed, LocalDateTime now) {
        Category category = new Category();
        category.setName(seed.name());
        category.setDescription(seed.description());
        category.setCreatedAt(now);
        category.setUpdatedAt(now);
        categoryRepository.save(category);
    }

    private void addMissingDescription(Category category, String description, LocalDateTime now) {
        if (!StringUtils.hasText(category.getDescription())) {
            category.setDescription(description);
            category.setUpdatedAt(now);
            categoryRepository.save(category);
        }
    }

    private record CategorySeed(String name, String description) {
    }
}
