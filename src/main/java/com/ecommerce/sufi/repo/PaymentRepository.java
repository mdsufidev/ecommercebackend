package com.ecommerce.sufi.repo;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.sufi.model.Payment;
public interface PaymentRepository extends JpaRepository<Payment, Long> {
 Optional<Payment> findByOrderIdAndOrderUserId(Long orderId, Long userId);
 Optional<Payment> findByGatewayOrderId(String gatewayOrderId);
}
