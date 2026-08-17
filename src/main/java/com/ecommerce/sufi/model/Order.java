package com.ecommerce.sufi.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor
public class Order {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
 @Enumerated(EnumType.STRING) @Column(nullable = false) private OrderStatus status = OrderStatus.PENDING;
 @Column(nullable = false, precision = 12, scale = 2) private BigDecimal total;
 @Column(nullable = false) private String shippingFullName;
 @Column(nullable = false) private String shippingPhone;
 @Column(nullable = false, length = 1000) private String shippingAddress;
 @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true) private List<OrderItem> items = new ArrayList<>();
 @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true) private Payment payment;
 private LocalDateTime createdAt; private LocalDateTime updatedAt;
 @PrePersist void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
 @PreUpdate void onUpdate() { updatedAt = LocalDateTime.now(); }
}
