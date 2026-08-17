package com.ecommerce.sufi.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity                 // Tells JPA that this class is a database entity
@Table(name="users")    // Table name (lowercase is standard convention in DB)
@Getter
@Setter
@NoArgsConstructor     // Default (empty) constructor required by JPA
@AllArgsConstructor    // All-arguments constructor
public class User {

	@Id                 // Marks this field as the Primary Key
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment ID in DB
	private Long id;

	private String name;

	@Column(nullable = false, unique = true)
	private String email;

	private String phone;

	@Column(nullable = false)
	private String password;
	private boolean enabled = true; // Default value true rakhna safe rehta hai
	@Column(nullable = false, columnDefinition = "integer default 0")
	private Integer authVersion = 0;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	// EAGER fetch fast authentication ke liye, HashSet null safety ke liye
	@ManyToMany(fetch = FetchType.EAGER)
	private Set<Role> roles = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (authVersion == null) authVersion = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
