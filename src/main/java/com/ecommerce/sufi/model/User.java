package com.ecommerce.sufi.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

@Entity                 // Tells JPA that this class is a database entity
@Table(name="users")    // Table name (lowercase is standard convention in DB)
@Data                   // Auto-generates Getters, Setters, toString, EqualsAndHashCode
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

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	// EAGER fetch fast authentication ke liye, HashSet null safety ke liye
	@ManyToMany(fetch = FetchType.EAGER)
	private Set<Role> roles = new HashSet<>();
}