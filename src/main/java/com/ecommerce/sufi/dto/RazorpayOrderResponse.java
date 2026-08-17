package com.ecommerce.sufi.dto;
public record RazorpayOrderResponse(String keyId,String gatewayOrderId,long amount,String currency,Long orderId,String customerName,String customerEmail,String customerPhone){}
