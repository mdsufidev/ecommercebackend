package com.ecommerce.sufi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.ecommerce.sufi.dto.PaymentResponse;
import com.ecommerce.sufi.dto.RazorpayOrderResponse;
import com.ecommerce.sufi.dto.RazorpayVerifyRequest;
import com.ecommerce.sufi.services.PaymentService;
import jakarta.validation.Valid;

@RestController @RequestMapping("/api/payments")
public class PaymentController {
 private final PaymentService paymentService;
 public PaymentController(PaymentService paymentService){this.paymentService=paymentService;}
 @GetMapping("/orders/{orderId}") public ResponseEntity<PaymentResponse> get(Authentication a,@PathVariable Long orderId){return ResponseEntity.ok(paymentService.getForOrder(a.getName(),orderId));}
 @PostMapping("/orders/{orderId}/razorpay") public ResponseEntity<RazorpayOrderResponse> initiate(Authentication a,@PathVariable Long orderId){return ResponseEntity.ok(paymentService.initiateRazorpay(a.getName(),orderId));}
 @PostMapping("/orders/{orderId}/razorpay/verify") public ResponseEntity<PaymentResponse> verify(Authentication a,@PathVariable Long orderId,@Valid @RequestBody RazorpayVerifyRequest request){return ResponseEntity.ok(paymentService.verifyRazorpay(a.getName(),orderId,request));}
 @PostMapping("/razorpay/webhook") public ResponseEntity<Void> webhook(@RequestHeader(value="X-Razorpay-Signature",required=false) String signature,@RequestBody String payload){paymentService.handleRazorpayWebhook(signature,payload);return ResponseEntity.ok().build();}
}
