package com.ecommerce.sufi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() { return "forward:/index.html"; }

    @GetMapping("/login")
    public String login() { return "forward:/login.html"; }

    @GetMapping("/register")
    public String register() { return "forward:/register.html"; }

    @GetMapping("/forgot-password")
    public String forgotPassword() { return "forward:/forgot-password.html"; }

    @GetMapping("/reset-password")
    public String resetPassword() { return "forward:/reset-password.html"; }

    @GetMapping("/products")
    public String products() { return "forward:/products.html"; }

    @GetMapping("/product")
    public String product() { return "forward:/product-detail.html"; }

    @GetMapping("/cart")
    public String cart() { return "forward:/cart.html"; }

    @GetMapping("/checkout")
    public String checkout() { return "forward:/checkout.html"; }

    @GetMapping("/addresses")
    public String addresses() { return "redirect:/products"; }

    @GetMapping("/profile")
    public String profile() { return "forward:/profile.html"; }

    @GetMapping("/my-orders")
    public String myOrders() { return "forward:/my-orders.html"; }

    @GetMapping("/order-detail")
    public String orderDetail() { return "forward:/order-detail.html"; }

    @GetMapping("/order-success")
    public String orderSuccess() { return "forward:/order-success.html"; }

    @GetMapping("/seller/dashboard")
    public String sellerDashboard() { return "forward:/seller/seller-dashboard.html"; }

    @GetMapping("/seller/products")
    public String sellerProducts() { return "forward:/seller/seller-products.html"; }

    @GetMapping("/seller/product-form")
    public String sellerProductForm() { return "forward:/seller/seller-add-product.html"; }

    @GetMapping("/seller/inventory")
    public String sellerInventory() { return "forward:/seller/seller-inventory.html"; }

    @GetMapping("/seller/orders")
    public String sellerOrders() { return "forward:/seller/seller-orders.html"; }
}
