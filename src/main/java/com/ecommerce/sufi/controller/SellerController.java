package com.ecommerce.sufi.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.sufi.dto.CsvImportResponse;
import com.ecommerce.sufi.dto.ProductRequest;
import com.ecommerce.sufi.dto.ProductResponse;
import com.ecommerce.sufi.dto.ProductResponseMapper;
import com.ecommerce.sufi.dto.SellerDashboardResponse;
import com.ecommerce.sufi.dto.SellerEarningsResponse;
import com.ecommerce.sufi.dto.UserProfileUpdateRequest;
import com.ecommerce.sufi.dto.UserResponse;
import com.ecommerce.sufi.model.User;
import com.ecommerce.sufi.services.UserService;
import com.ecommerce.sufi.dto.SellerOrderResponse;
import com.ecommerce.sufi.dto.OrderItemStatusUpdateRequest;
import com.ecommerce.sufi.dto.StockUpdateRequest;
import com.ecommerce.sufi.services.OrderService;
import com.ecommerce.sufi.services.ProductService;
import com.ecommerce.sufi.services.SellerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/seller")
public class SellerController {
    private final SellerService sellerService;
    private final ProductService productService;
    private final OrderService orderService;
    private final UserService userService;

    public SellerController(SellerService sellerService, ProductService productService, OrderService orderService,
            UserService userService) {
        this.sellerService = sellerService;
        this.productService = productService;
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<SellerDashboardResponse> dashboard(Authentication authentication) {
        return ResponseEntity.ok(sellerService.getDashboard(authentication.getName()));
    }

    @GetMapping("/earnings")
    public ResponseEntity<SellerEarningsResponse> earnings(Authentication authentication) {
        return ResponseEntity.ok(sellerService.getEarnings(authentication.getName()));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> profile(Authentication authentication) {
        return ResponseEntity.ok(toUserResponse(userService.getUserByEmail(authentication.getName())));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(Authentication authentication,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        return ResponseEntity.ok(toUserResponse(userService.updateProfile(authentication.getName(), request)));
    }

    @GetMapping("/products")
    public ResponseEntity<Page<ProductResponse>> products(Authentication authentication,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.getSellerProducts(authentication.getName(), page, size)
                .map(ProductResponseMapper::from));
    }

    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(Authentication authentication,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductResponseMapper.from(productService.createProduct(request, authentication.getName())));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductResponse> updateProduct(Authentication authentication, @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ProductResponseMapper.from(productService.updateProduct(id, request, authentication.getName())));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(Authentication authentication, @PathVariable Long id) {
        productService.deleteProduct(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/products/{id}/stock")
    public ResponseEntity<ProductResponse> updateStock(Authentication authentication, @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(ProductResponseMapper.from(
                productService.updateSellerStock(id, request.stock(), authentication.getName())));
    }

    @PostMapping("/products/image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new com.ecommerce.sufi.exception.BadRequestException("Please upload a valid image file");
        }
        try {
            String original = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
            String extension = original.contains(".") ? original.substring(original.lastIndexOf('.')).toLowerCase(Locale.ROOT) : "";
            if (!List.of(".jpg", ".jpeg", ".png", ".webp", ".gif").contains(extension)) {
                throw new com.ecommerce.sufi.exception.BadRequestException("Supported images: JPG, PNG, WEBP or GIF");
            }
            Path directory = Paths.get("uploads", "products").toAbsolutePath().normalize();
            Files.createDirectories(directory);
            String filename = UUID.randomUUID() + extension;
            Files.copy(file.getInputStream(), directory.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("imageUrl", "/uploads/products/" + filename));
        } catch (java.io.IOException exception) {
            throw new com.ecommerce.sufi.exception.BadRequestException("Could not save image");
        }
    }

    @PostMapping("/products/import")
    public ResponseEntity<CsvImportResponse> importProducts(Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new com.ecommerce.sufi.exception.BadRequestException("CSV file is required");
        }
        int imported = 0;
        List<String> errors = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new com.ecommerce.sufi.exception.BadRequestException("CSV file is empty");
            }
            List<String> headers = parseCsvLine(headerLine).stream()
                    .map(value -> value.trim().toLowerCase(Locale.ROOT)).toList();
            for (String required : List.of("name", "sku", "price", "stock", "categoryid")) {
                if (!headers.contains(required)) {
                    throw new com.ecommerce.sufi.exception.BadRequestException("Missing CSV column: " + required);
                }
            }
            String line;
            int row = 1;
            while ((line = reader.readLine()) != null) {
                row++;
                if (line.isBlank()) continue;
                try {
                    List<String> values = parseCsvLine(line);
                    Map<String, String> data = java.util.stream.IntStream.range(0, headers.size()).boxed()
                            .collect(Collectors.toMap(headers::get, index -> index < values.size() ? values.get(index).trim() : ""));
                    ProductRequest request = new ProductRequest();
                    request.setName(data.get("name"));
                    request.setSku(data.get("sku"));
                    request.setPrice(new BigDecimal(data.get("price")));
                    request.setStock(Integer.valueOf(data.get("stock")));
                    request.setCategoryId(Long.valueOf(data.get("categoryid")));
                    request.setDescription(data.getOrDefault("description", ""));
                    request.setImageUrl(data.getOrDefault("imageurl", ""));
                    productService.createProduct(request, authentication.getName());
                    imported++;
                } catch (RuntimeException exception) {
                    errors.add("Row " + row + ": " + exception.getMessage());
                }
            }
        } catch (java.io.IOException exception) {
            throw new com.ecommerce.sufi.exception.BadRequestException("Could not read CSV file");
        }
        return ResponseEntity.ok(new CsvImportResponse(imported, errors));
    }

    private static List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (character == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    value.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ',' && !quoted) {
                values.add(value.toString());
                value.setLength(0);
            } else {
                value.append(character);
            }
        }
        values.add(value.toString());
        return values;
    }

    @GetMapping("/orders")
    public ResponseEntity<List<SellerOrderResponse>> orders(Authentication authentication) {
        return ResponseEntity.ok(orderService.getSellerOrders(authentication.getName()));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<SellerOrderResponse> order(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getSellerOrder(authentication.getName(), id));
    }

    @PutMapping("/orders/{orderId}/items/{itemId}/status")
    public ResponseEntity<SellerOrderResponse> updateItemStatus(Authentication authentication,
            @PathVariable Long orderId, @PathVariable Long itemId,
            @Valid @RequestBody OrderItemStatusUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateSellerItemStatus(
                authentication.getName(), orderId, itemId, request.status()));
    }

    @PatchMapping("/orders/{orderItemId}/status")
    public ResponseEntity<SellerOrderResponse> updateItemStatus(Authentication authentication,
            @PathVariable Long orderItemId, @Valid @RequestBody OrderItemStatusUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateSellerItemStatus(
                authentication.getName(), orderItemId, request.status()));
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone(),
                user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toUnmodifiableSet()),
                user.isEnabled(),
                user.getCreatedAt());
    }
}
