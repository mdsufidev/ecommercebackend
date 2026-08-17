package com.ecommerce.sufi.controller;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.sufi.dto.OrderRequest;
import com.ecommerce.sufi.dto.OrderResponse;
import com.ecommerce.sufi.services.OrderService;

import jakarta.validation.Valid;
@RestController @RequestMapping("/api/orders")
public class OrderController {
 private final OrderService orderService; public OrderController(OrderService orderService){this.orderService=orderService;}
 @PostMapping public ResponseEntity<OrderResponse> checkout(Authentication a,@Valid @RequestBody OrderRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(orderService.checkout(a.getName(),r));}
 @GetMapping("/my") public ResponseEntity<List<OrderResponse>> mine(Authentication a){return ResponseEntity.ok(orderService.getMyOrders(a.getName()));}
 @GetMapping("/{id}") public ResponseEntity<OrderResponse> one(Authentication a,@PathVariable Long id){return ResponseEntity.ok(orderService.getOrder(a.getName(),id));}
 @DeleteMapping("/{id}") public ResponseEntity<Void> cancel(Authentication a,@PathVariable Long id){orderService.cancelOrder(a.getName(),id);return ResponseEntity.noContent().build();}
}
