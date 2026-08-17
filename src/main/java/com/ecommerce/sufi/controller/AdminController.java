package com.ecommerce.sufi.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.sufi.dto.AdminDashboardResponse;
import com.ecommerce.sufi.dto.AdminUserCreateRequest;
import com.ecommerce.sufi.dto.AdminUserUpdateRequest;
import com.ecommerce.sufi.dto.OrderResponse;
import com.ecommerce.sufi.dto.OrderStatusUpdateRequest;
import com.ecommerce.sufi.dto.ProductRequest;
import com.ecommerce.sufi.dto.ProductResponse;
import com.ecommerce.sufi.dto.ProductRejectionRequest;
import com.ecommerce.sufi.dto.ProductResponseMapper;
import com.ecommerce.sufi.dto.UserResponse;
import com.ecommerce.sufi.dto.CategoryRequest;
import com.ecommerce.sufi.dto.UserRolesUpdateRequest;
import com.ecommerce.sufi.model.Category;
import com.ecommerce.sufi.model.Product;
import com.ecommerce.sufi.model.User;
import com.ecommerce.sufi.services.AdminService;
import com.ecommerce.sufi.services.CategoryService;
import com.ecommerce.sufi.services.OrderService;
import com.ecommerce.sufi.services.ProductService;
import com.ecommerce.sufi.services.SellerService;
import com.ecommerce.sufi.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final ProductService productService;
    private final OrderService orderService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final SellerService sellerService;

    public AdminController(
            AdminService adminService,
            ProductService productService,
            OrderService orderService,
            CategoryService categoryService,
            UserService userService,
            SellerService sellerService) {

        this.adminService = adminService;
        this.productService = productService;
        this.orderService = orderService;
        this.categoryService = categoryService;
        this.userService = userService;
        this.sellerService = sellerService;
    }


    // =====================================================
    // DASHBOARD
    // =====================================================

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> dashboard() {

        return ResponseEntity.ok(
                adminService.getDashboard()
        );
    }


    // =====================================================
    // USERS
    // =====================================================

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> users(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                adminService.getUsers(page, size)
        );
    }


    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> user(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                adminService.getUser(id)
        );
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody AdminUserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUser(request));
    }


    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserUpdateRequest request) {

        return ResponseEntity.ok(
                adminService.updateUser(id, request)
        );
    }


    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id) {

        adminService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{id}/enabled")
    public ResponseEntity<UserResponse> setUserEnabled(@PathVariable Long id,
            @RequestBody java.util.Map<String, Boolean> request) {
        return ResponseEntity.ok(adminService.setUserEnabled(id, Boolean.TRUE.equals(request.get("enabled"))));
    }

    @PatchMapping("/users/{id}/roles")
    public ResponseEntity<UserResponse> setUserRoles(@PathVariable Long id,
            @Valid @RequestBody UserRolesUpdateRequest request) {
        return ResponseEntity.ok(adminService.setUserRoles(id, request.roles()));
    }

    @GetMapping("/sellers")
    public ResponseEntity<Page<UserResponse>> sellers(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getSellers(page, size));
    }

    @GetMapping("/sellers/{id}/products")
    public ResponseEntity<Page<ProductResponse>> sellerProducts(@PathVariable Long id,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size) {
        User seller = userService.getUserById(id);
        return ResponseEntity.ok(productService.getSellerProducts(seller.getEmail(), page, size)
                .map(ProductResponseMapper::from));
    }

    @GetMapping("/sellers/{id}/orders")
    public ResponseEntity<List<com.ecommerce.sufi.dto.SellerOrderResponse>> sellerOrders(@PathVariable Long id) {
        User seller = userService.getUserById(id);
        return ResponseEntity.ok(orderService.getSellerOrders(seller.getEmail()));
    }

    @GetMapping("/sellers/{id}/overview")
    public ResponseEntity<com.ecommerce.sufi.dto.SellerDashboardResponse> sellerOverview(@PathVariable Long id) {
        User seller = userService.getUserById(id);
        return ResponseEntity.ok(sellerService.getDashboard(seller.getEmail()));
    }


    // =====================================================
    // PRODUCTS
    // =====================================================

    @GetMapping("/products")
    public ResponseEntity<Page<ProductResponse>> products(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                productService
                        .getAllProductsForAdmin(page, size)
                        .map(ProductResponseMapper::from)
        );
    }


    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> product(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ProductResponseMapper.from(
                        productService.getProductForAdmin(id)
                )
        );
    }


    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request) {

        Product product =
                productService.createProductByAdmin(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProductResponseMapper.from(product));
    }


    @PutMapping("/products/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {

        Product product =
                productService.updateProductByAdmin(id, request);

        return ResponseEntity.ok(
                ProductResponseMapper.from(product)
        );
    }


    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id) {

        productService.deleteProductByAdmin(id);

        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/products/{id}/approve")
    public ResponseEntity<ProductResponse> approveProduct(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ProductResponseMapper.from(
                        productService.approveProduct(id)
                )
        );
    }


    @PatchMapping("/products/{id}/reject")
    public ResponseEntity<ProductResponse> rejectProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRejectionRequest request) {

        return ResponseEntity.ok(
                ProductResponseMapper.from(
                        productService.rejectProduct(id, request.reason())
                )
        );
    }


    // =====================================================
    // CATEGORIES
    // =====================================================

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> categories() {

        return ResponseEntity.ok(
                categoryService.getAllCategories()
        );
    }


    @GetMapping("/categories/{id}")
    public ResponseEntity<Category> category(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                categoryService.getCategoryById(id)
        );
    }


    @PostMapping("/categories")
    public ResponseEntity<Category> createCategory(
            @Valid @RequestBody CategoryRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryService.createCategory(request));
    }


    @PutMapping("/categories/{id}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {

        return ResponseEntity.ok(
                categoryService.updateCategory(id, request)
        );
    }


    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id) {

        categoryService.deleteCategory(id);

        return ResponseEntity.noContent().build();
    }


    // =====================================================
    // ORDERS
    // =====================================================

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> orders() {

        return ResponseEntity.ok(
                orderService.getAllOrders()
        );
    }


    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> order(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                orderService.getOrderForAdmin(id)
        );
    }


    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {

        return ResponseEntity.ok(
                orderService.updateStatus(
                        id,
                        request.status()
                )
        );
    }
}
