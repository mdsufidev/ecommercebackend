package com.ecommerce.sufi.services;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.sufi.dto.OrderRequest;
import com.ecommerce.sufi.dto.OrderResponse;
import com.ecommerce.sufi.dto.PaymentResponse;
import com.ecommerce.sufi.dto.SellerOrderResponse;
import com.ecommerce.sufi.exception.BadRequestException;
import com.ecommerce.sufi.exception.ResourceNotFoundException;
import com.ecommerce.sufi.model.Address;
import com.ecommerce.sufi.model.Cart;
import com.ecommerce.sufi.model.CartItem;
import com.ecommerce.sufi.model.Order;
import com.ecommerce.sufi.model.OrderItem;
import com.ecommerce.sufi.model.OrderStatus;
import com.ecommerce.sufi.model.OrderItemStatus;
import com.ecommerce.sufi.model.Payment;
import com.ecommerce.sufi.model.PaymentStatus;
import com.ecommerce.sufi.model.Product;
import com.ecommerce.sufi.model.ProductStatus;
import com.ecommerce.sufi.model.User;
import com.ecommerce.sufi.repo.AddressRepository;
import com.ecommerce.sufi.repo.CartRepository;
import com.ecommerce.sufi.repo.OrderRepository;
import com.ecommerce.sufi.repo.OrderItemRepository;
import com.ecommerce.sufi.repo.PaymentRepository;
import com.ecommerce.sufi.repo.ProductRepository;

@Service @Transactional
public class OrderServiceImpl implements OrderService {
 private final OrderRepository orderRepository; private final CartRepository cartRepository; private final AddressRepository addressRepository;
 private final ProductRepository productRepository; private final UserService userService; private final PaymentRepository paymentRepository;
 private final OrderItemRepository orderItemRepository;
 public OrderServiceImpl(OrderRepository orderRepository, CartRepository cartRepository, AddressRepository addressRepository,
   ProductRepository productRepository, UserService userService, PaymentRepository paymentRepository,
   OrderItemRepository orderItemRepository) {
  this.orderRepository=orderRepository; this.cartRepository=cartRepository; this.addressRepository=addressRepository;
  this.productRepository=productRepository; this.userService=userService; this.paymentRepository=paymentRepository;
  this.orderItemRepository=orderItemRepository;
 }
 @Override public OrderResponse checkout(String email, OrderRequest request) {
  User user=userService.getUserByEmail(email);
  Address address=addressRepository.findByIdAndUserId(request.addressId(), user.getId()).orElseThrow(() -> new ResourceNotFoundException("Address not found"));
  Cart cart=cartRepository.findByUserId(user.getId()).orElseThrow(() -> new BadRequestException("Cart is empty"));
  if(cart.getItems().isEmpty()) {
	throw new BadRequestException("Cart is empty");
  }
  Order order=new Order(); order.setUser(user); order.setShippingFullName(address.getFullName()); order.setShippingPhone(address.getPhone());
  order.setShippingAddress(formatAddress(address)); BigDecimal total=BigDecimal.ZERO;
  for(CartItem cartItem: cart.getItems()) {
   Product product=productRepository.findByIdAndStatus(cartItem.getProduct().getId(), ProductStatus.APPROVED)
    .orElseThrow(() -> new BadRequestException("A cart product is no longer available"));
   if(product.getStock()<cartItem.getQuantity()) {
	throw new BadRequestException("Insufficient stock for " + product.getName());
   }
   BigDecimal subtotal=product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
   OrderItem item=new OrderItem(); item.setOrder(order); item.setProduct(product); item.setProductName(product.getName());
   item.setUnitPrice(product.getPrice()); item.setQuantity(cartItem.getQuantity()); item.setSubtotal(subtotal);
   item.setStatus(OrderItemStatus.PLACED); order.getItems().add(item);
   product.setStock(product.getStock()-cartItem.getQuantity()); total=total.add(subtotal);
  }
  order.setTotal(total); Order saved=orderRepository.save(order);
  Payment payment=new Payment(); payment.setOrder(saved); payment.setMethod(request.paymentMethod()); payment.setStatus(PaymentStatus.PENDING); payment.setAmount(total);
  saved.setPayment(paymentRepository.save(payment)); cart.getItems().clear();
  return toResponse(saved);
 }
 @Override @Transactional(readOnly=true) public List<OrderResponse> getMyOrders(String email) { User user=userService.getUserByEmail(email); return orderRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(this::toResponse).toList(); }
 @Override @Transactional(readOnly=true) public OrderResponse getOrder(String email, Long id) { User user=userService.getUserByEmail(email); return toResponse(orderRepository.findByIdAndUserId(id,user.getId()).orElseThrow(() -> new ResourceNotFoundException("Order not found"))); }
 @Override public void cancelOrder(String email, Long id) { User user=userService.getUserByEmail(email); Order order=orderRepository.findByIdAndUserId(id,user.getId()).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
  if(order.getStatus()!=OrderStatus.PENDING && order.getStatus()!=OrderStatus.CONFIRMED) {
	throw new BadRequestException("This order can no longer be cancelled");
  }
  order.setStatus(OrderStatus.CANCELLED); for(OrderItem item:order.getItems()){
   if(item.getStatus()!=OrderItemStatus.CANCELLED && item.getStatus()!=OrderItemStatus.RETURNED && !item.isStockRestored()) {
    Product product=item.getProduct(); product.setStock(product.getStock()+item.getQuantity()); item.setStockRestored(true);
   }
   item.setStatus(OrderItemStatus.CANCELLED);
  }
  if(order.getPayment().getStatus()==PaymentStatus.SUCCESS) {
	order.getPayment().setStatus(PaymentStatus.REFUNDED);
  } }
 @Override @Transactional(readOnly=true) public List<OrderResponse> getAllOrders() { return orderRepository.findAll().stream().map(this::toResponse).toList(); }
 @Override @Transactional(readOnly=true) public OrderResponse getOrderForAdmin(Long id) { return toResponse(orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found"))); }
 @Override public OrderResponse updateStatus(Long id, OrderStatus status) { Order order=orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
  if(order.getStatus()==OrderStatus.CANCELLED) {
	throw new BadRequestException("Cancelled orders cannot be updated");
  } if(status==OrderStatus.CANCELLED) {
	order.setStatus(OrderStatus.CANCELLED);
	for(OrderItem item:order.getItems()) {
	 if(item.getStatus()!=OrderItemStatus.CANCELLED && item.getStatus()!=OrderItemStatus.RETURNED && !item.isStockRestored()) {
	  Product product=item.getProduct(); product.setStock(product.getStock()+item.getQuantity()); item.setStockRestored(true);
	 }
	 item.setStatus(OrderItemStatus.CANCELLED);
	}
	if(order.getPayment()!=null && order.getPayment().getStatus()==PaymentStatus.SUCCESS) {
	 order.getPayment().setStatus(PaymentStatus.REFUNDED);
	}
	return toResponse(order);
  } order.setStatus(status); return toResponse(order); }
 @Override @Transactional(readOnly=true) public List<SellerOrderResponse> getSellerOrders(String email) {
  Long sellerId=userService.getUserByEmail(email).getId();
  return orderRepository.findDistinctByItemsProductSellerIdOrderByCreatedAtDesc(sellerId).stream().map(order -> toSellerResponse(order,sellerId)).toList();
 }
 @Override @Transactional(readOnly=true) public SellerOrderResponse getSellerOrder(String email, Long id) {
  Long sellerId=userService.getUserByEmail(email).getId();
  Order order=orderRepository.findDistinctByIdAndItemsProductSellerId(id,sellerId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
  return toSellerResponse(order,sellerId);
 }
 @Override public SellerOrderResponse updateSellerItemStatus(String email, Long orderId, Long itemId, OrderItemStatus status) {
  Long sellerId=userService.getUserByEmail(email).getId();
  OrderItem item=orderItemRepository.findByIdAndOrderIdAndProductSellerId(itemId,orderId,sellerId)
   .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));
  return advanceSellerItem(item,sellerId,status);
 }
 @Override public SellerOrderResponse updateSellerItemStatus(String email, Long itemId, OrderItemStatus status) {
  Long sellerId=userService.getUserByEmail(email).getId();
  OrderItem item=orderItemRepository.findByIdAndProductSellerId(itemId,sellerId)
   .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));
  return advanceSellerItem(item,sellerId,status);
 }
 private SellerOrderResponse advanceSellerItem(OrderItem item, Long sellerId, OrderItemStatus status) {
  if(status==OrderItemStatus.CANCELLED || status==OrderItemStatus.RETURNED) {
   throw new BadRequestException("Seller cannot set cancelled or returned status");
  }
  List<OrderItemStatus> flow=List.of(OrderItemStatus.PLACED,OrderItemStatus.CONFIRMED,OrderItemStatus.PACKED,
   OrderItemStatus.SHIPPED,OrderItemStatus.DELIVERED);
  int current=flow.indexOf(item.getStatus()); int requested=flow.indexOf(status);
  if(current<0 || requested!=current+1) {
   throw new BadRequestException("Status must follow PLACED → CONFIRMED → PACKED → SHIPPED → DELIVERED");
  }
  item.setStatus(status);
  synchronizeOrderStatus(item.getOrder());
  return toSellerResponse(item.getOrder(),sellerId);
 }
 private void synchronizeOrderStatus(Order order) {
  if(order.getStatus()==OrderStatus.CANCELLED)return;
  List<OrderItemStatus> active=order.getItems().stream().map(OrderItem::getStatus)
   .filter(status->status!=OrderItemStatus.CANCELLED&&status!=OrderItemStatus.RETURNED).toList();
  if(active.isEmpty()){order.setStatus(OrderStatus.CANCELLED);return;}
  List<OrderItemStatus> flow=List.of(OrderItemStatus.PLACED,OrderItemStatus.CONFIRMED,OrderItemStatus.PACKED,
   OrderItemStatus.SHIPPED,OrderItemStatus.DELIVERED);
  int slowest=active.stream().mapToInt(flow::indexOf).min().orElse(0);
  OrderStatus aggregate=switch(flow.get(Math.max(0,slowest))){
   case PLACED -> OrderStatus.PENDING;
   case CONFIRMED -> OrderStatus.CONFIRMED;
   case PACKED -> OrderStatus.PROCESSING;
   case SHIPPED -> OrderStatus.SHIPPED;
   case DELIVERED -> OrderStatus.DELIVERED;
   default -> OrderStatus.PENDING;
  };
  order.setStatus(aggregate);
 }
 private String formatAddress(Address a) { return String.join(", ", a.getAddressLine1(), a.getAddressLine2()==null?"":a.getAddressLine2(), a.getCity(), a.getState(), a.getPincode(), a.getCountry()).replace(", ,", ","); }
 private OrderResponse toResponse(Order o) { List<OrderResponse.Item> items=o.getItems().stream().map(i -> new OrderResponse.Item(i.getProduct().getId(),i.getProductName(),i.getProduct().getImageUrl(),i.getUnitPrice(),i.getQuantity(),i.getSubtotal(),i.getStatus())).toList(); Payment p=o.getPayment(); PaymentResponse payment=p==null?null:new PaymentResponse(p.getId(),p.getMethod(),p.getStatus(),p.getAmount()); return new OrderResponse(o.getId(),o.getStatus(),o.getTotal(),o.getShippingFullName(),o.getShippingPhone(),o.getShippingAddress(),items,payment,o.getCreatedAt()); }
 private SellerOrderResponse toSellerResponse(Order order, Long sellerId) {
  List<SellerOrderResponse.Item> items=order.getItems().stream().filter(item -> item.getProduct().getSeller()!=null&&item.getProduct().getSeller().getId().equals(sellerId))
   .map(item -> new SellerOrderResponse.Item(item.getId(),item.getProduct().getId(),item.getProductName(),item.getUnitPrice(),item.getQuantity(),item.getSubtotal(),item.getStatus())).toList();
  BigDecimal sellerTotal=items.stream().map(SellerOrderResponse.Item::subtotal).reduce(BigDecimal.ZERO,BigDecimal::add);
  return new SellerOrderResponse(order.getId(),order.getStatus(),order.getUser().getName(),order.getShippingPhone(),order.getShippingAddress(),items,sellerTotal,order.getCreatedAt());
 }
}
