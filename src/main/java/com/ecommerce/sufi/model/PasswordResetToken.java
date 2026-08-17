package com.ecommerce.sufi.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name="password_reset_tokens") @Getter @Setter @NoArgsConstructor
public class PasswordResetToken {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,unique=true,length=64) private String tokenHash;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
    @Column(nullable=false) private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    @Column(nullable=false) private LocalDateTime createdAt;
    @PrePersist void onCreate(){if(createdAt==null)createdAt=LocalDateTime.now();}
}
