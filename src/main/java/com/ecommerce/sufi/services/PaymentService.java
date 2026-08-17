package com.ecommerce.sufi.services;
import com.ecommerce.sufi.dto.PaymentResponse;
import com.ecommerce.sufi.dto.RazorpayOrderResponse;
import com.ecommerce.sufi.dto.RazorpayVerifyRequest;
public interface PaymentService {
 PaymentResponse getForOrder(String email, Long orderId);
 RazorpayOrderResponse initiateRazorpay(String email,Long orderId);
 PaymentResponse verifyRazorpay(String email,Long orderId,RazorpayVerifyRequest request);
 void handleRazorpayWebhook(String signature,String payload);
}
