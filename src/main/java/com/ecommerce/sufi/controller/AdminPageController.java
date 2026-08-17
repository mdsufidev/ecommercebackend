package com.ecommerce.sufi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {
    @GetMapping({"/admin", "/admin/dashboard"}) public String dashboard() { return "admin/dashboard"; }
    @GetMapping("/admin/products") public String products() { return "admin/products"; }
    @GetMapping("/admin/categories") public String categories() { return "admin/categories"; }
    @GetMapping("/admin/orders") public String orders() { return "admin/orders"; }
    @GetMapping("/admin/users") public String users() { return "admin/users"; }
    @GetMapping("/admin/sellers") public String sellers() { return "admin/sellers"; }
}
