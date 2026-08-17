package com.ecommerce.sufi.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ecommerce.sufi.dto.PaymentResponse;
import com.ecommerce.sufi.dto.RazorpayOrderResponse;
import com.ecommerce.sufi.dto.RazorpayVerifyRequest;
import com.ecommerce.sufi.exception.BadRequestException;
import com.ecommerce.sufi.exception.ResourceNotFoundException;
import com.ecommerce.sufi.model.Payment;
import com.ecommerce.sufi.model.PaymentMethod;
import com.ecommerce.sufi.model.PaymentStatus;
import com.ecommerce.sufi.model.User;
import com.ecommerce.sufi.repo.PaymentRepository;

@Service
@Transactional(readOnly=true)
public class PaymentServiceImpl implements PaymentService {
 private final PaymentRepository paymentRepository;
 private final UserService userService;
 private final ObjectMapper objectMapper;
 private final HttpClient httpClient=HttpClient.newHttpClient();
 private final String keyId;
 private final String keySecret;
 private final String webhookSecret;

 public PaymentServiceImpl(PaymentRepository paymentRepository,UserService userService,ObjectMapper objectMapper,
   @Value("${razorpay.key-id:}") String keyId,@Value("${razorpay.key-secret:}") String keySecret,
   @Value("${razorpay.webhook-secret:}") String webhookSecret){
  this.paymentRepository=paymentRepository;this.userService=userService;this.objectMapper=objectMapper;
  this.keyId=keyId;this.keySecret=keySecret;this.webhookSecret=webhookSecret;
 }

 @Override public PaymentResponse getForOrder(String email,Long orderId){return response(ownedPayment(email,orderId));}

 @Override @Transactional
 public RazorpayOrderResponse initiateRazorpay(String email,Long orderId){
  requireConfigured(); Payment payment=ownedPayment(email,orderId);
  if(payment.getMethod()!=PaymentMethod.ONLINE)throw new BadRequestException("This order uses cash on delivery");
  if(payment.getStatus()==PaymentStatus.SUCCESS)throw new BadRequestException("This order is already paid");
  long amount=payment.getAmount().movePointRight(2).longValueExact();
  if(payment.getGatewayOrderId()==null){
   try{
    String body=objectMapper.createObjectNode().put("amount",amount).put("currency","INR")
      .put("receipt","order_"+payment.getOrder().getId()).put("partial_payment",false).toString();
    HttpRequest request=HttpRequest.newBuilder(URI.create("https://api.razorpay.com/v1/orders"))
      .header("Authorization",basicAuth()).header("Content-Type","application/json")
      .POST(HttpRequest.BodyPublishers.ofString(body)).build();
    HttpResponse<String> gateway=httpClient.send(request,HttpResponse.BodyHandlers.ofString());
    if(gateway.statusCode()/100!=2)throw new BadRequestException("Payment gateway could not create an order");
    JsonNode json=objectMapper.readTree(gateway.body());
    payment.setGatewayOrderId(json.path("id").asText());payment.setStatus(PaymentStatus.PENDING);
    paymentRepository.save(payment);
   }catch(BadRequestException exception){throw exception;}catch(Exception exception){throw new BadRequestException("Unable to connect to payment gateway");}
  }
  User user=payment.getOrder().getUser();
  return new RazorpayOrderResponse(keyId,payment.getGatewayOrderId(),amount,"INR",payment.getOrder().getId(),
    user.getName(),user.getEmail(),user.getPhone());
 }

 @Override @Transactional
 public PaymentResponse verifyRazorpay(String email,Long orderId,RazorpayVerifyRequest request){
  requireConfigured();Payment payment=ownedPayment(email,orderId);
  if(payment.getMethod()!=PaymentMethod.ONLINE)throw new BadRequestException("This order is not an online payment");
  if(!request.razorpayOrderId().equals(payment.getGatewayOrderId()))throw new BadRequestException("Payment order does not match");
  String expected=hmac(payment.getGatewayOrderId()+"|"+request.razorpayPaymentId(),keySecret);
  if(!constantTimeEquals(expected,request.razorpaySignature()))throw new BadRequestException("Payment signature verification failed");
  JsonNode gateway=fetchPayment(request.razorpayPaymentId());
  long expectedAmount=payment.getAmount().movePointRight(2).longValueExact();
  if(!"captured".equals(gateway.path("status").asText()) || expectedAmount!=gateway.path("amount").asLong()
      || !payment.getGatewayOrderId().equals(gateway.path("order_id").asText())){
   throw new BadRequestException("Payment has not been captured");
  }
  payment.setGatewayPaymentId(request.razorpayPaymentId());payment.setGatewaySignature(request.razorpaySignature());
  payment.setStatus(PaymentStatus.SUCCESS);return response(paymentRepository.save(payment));
 }

 @Override @Transactional
 public void handleRazorpayWebhook(String signature,String payload){
  if(webhookSecret.isBlank()||signature==null||!constantTimeEquals(hmac(payload,webhookSecret),signature))
   throw new BadRequestException("Invalid webhook signature");
  try{
   JsonNode root=objectMapper.readTree(payload),entity=root.path("payload").path("payment").path("entity");
   String gatewayOrderId=entity.path("order_id").asText(null);if(gatewayOrderId==null)return;
   Payment payment=paymentRepository.findByGatewayOrderId(gatewayOrderId).orElse(null);if(payment==null)return;
   String event=root.path("event").asText();long expectedAmount=payment.getAmount().movePointRight(2).longValueExact();
   if("payment.captured".equals(event)&&entity.path("amount").asLong()==expectedAmount){
    payment.setGatewayPaymentId(entity.path("id").asText());payment.setStatus(PaymentStatus.SUCCESS);paymentRepository.save(payment);
   }else if("payment.failed".equals(event)&&payment.getStatus()!=PaymentStatus.SUCCESS){
    payment.setGatewayPaymentId(entity.path("id").asText());payment.setStatus(PaymentStatus.FAILED);paymentRepository.save(payment);
   }
  }catch(BadRequestException exception){throw exception;}catch(Exception exception){throw new BadRequestException("Invalid webhook payload");}
 }

 private JsonNode fetchPayment(String paymentId){
  try{HttpRequest request=HttpRequest.newBuilder(URI.create("https://api.razorpay.com/v1/payments/"+paymentId))
    .header("Authorization",basicAuth()).GET().build();HttpResponse<String> response=httpClient.send(request,HttpResponse.BodyHandlers.ofString());
   if(response.statusCode()/100!=2)throw new BadRequestException("Could not verify payment status");return objectMapper.readTree(response.body());
  }catch(BadRequestException exception){throw exception;}catch(Exception exception){throw new BadRequestException("Unable to verify payment with gateway");}
 }
 private Payment ownedPayment(String email,Long orderId){Long userId=userService.getUserByEmail(email).getId();return paymentRepository.findByOrderIdAndOrderUserId(orderId,userId).orElseThrow(()->new ResourceNotFoundException("Payment not found"));}
 private PaymentResponse response(Payment p){return new PaymentResponse(p.getId(),p.getMethod(),p.getStatus(),p.getAmount());}
 private void requireConfigured(){if(keyId.isBlank()||keySecret.isBlank())throw new BadRequestException("Online payment is not configured");}
 private String basicAuth(){return "Basic "+Base64.getEncoder().encodeToString((keyId+":"+keySecret).getBytes(StandardCharsets.UTF_8));}
 private String hmac(String value,String secret){try{Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception exception){throw new IllegalStateException("Could not verify gateway signature",exception);}}
 private boolean constantTimeEquals(String left,String right){return right!=null&&java.security.MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8),right.getBytes(StandardCharsets.UTF_8));}
}
