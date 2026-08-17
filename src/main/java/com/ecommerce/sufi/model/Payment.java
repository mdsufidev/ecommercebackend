package com.ecommerce.sufi.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity @Table(name = "payments") @Getter @Setter @NoArgsConstructor
public class Payment {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
 @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "order_id", nullable = false, unique = true) private Order order;
 @Enumerated(EnumType.STRING) @Column(nullable = false) private PaymentMethod method;
 @Enumerated(EnumType.STRING) @Column(nullable = false) private PaymentStatus status;
 @Column(nullable = false, precision = 12, scale = 2) private BigDecimal amount;
 @Column(unique = true, length = 100) private String gatewayOrderId;
 @Column(unique = true, length = 100) private String gatewayPaymentId;
 @Column(length = 200) private String gatewaySignature;
 private LocalDateTime createdAt;
 private LocalDateTime updatedAt;
 @PrePersist void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
 @jakarta.persistence.PreUpdate void onUpdate() { updatedAt = LocalDateTime.now(); }
}
