package com.ecommerce.sufi.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .formLogin(form -> form.disable())

            .httpBasic(basic -> basic.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth

            	    // =========================
            	    // AUTH
            	    // =========================

            	    .requestMatchers(
            	        "/api/auth/register",
            	        "/api/auth/login"
            	    ).permitAll()


            	    // =========================
            	    // CATEGORIES
            	    // =========================

            	    .requestMatchers(
            	        HttpMethod.GET,
            	        "/api/categories/**"
            	    ).permitAll()

            	    .requestMatchers(
            	        HttpMethod.POST,
            	        "/api/categories/**"
            	    ).hasRole("ADMIN")

            	    .requestMatchers(
            	        HttpMethod.PUT,
            	        "/api/categories/**"
            	    ).hasRole("ADMIN")

            	    .requestMatchers(
            	        HttpMethod.DELETE,
            	        "/api/categories/**"
            	    ).hasRole("ADMIN")


            	    // =========================
            	    // PRODUCTS
            	    // =========================

            	    .requestMatchers(
            	        HttpMethod.GET,
            	        "/api/products/**"
            	    ).permitAll()

            	    .requestMatchers(
            	        HttpMethod.POST,
            	        "/api/products/**"
            	    ).hasAnyRole("SELLER", "ADMIN")

            	    .requestMatchers(
            	        HttpMethod.PATCH,
            	        "/api/products/*/approve"
            	    ).hasRole("ADMIN")

            	    .requestMatchers(
            	        HttpMethod.PATCH,
            	        "/api/products/*/reject"
            	    ).hasRole("ADMIN")

            	    // PUT & DELETE are authenticated.
            	    // Owner/Admin check happens in ProductService.
            	    .requestMatchers(
            	        HttpMethod.PUT,
            	        "/api/products/**"
            	    ).authenticated()

            	    .requestMatchers(
            	        HttpMethod.DELETE,
            	        "/api/products/**"
            	    ).authenticated()


            	    // =========================
            	    // EVERYTHING ELSE
            	    // =========================

            	    .anyRequest().authenticated()
            	)

            .addFilterBefore(
                jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}