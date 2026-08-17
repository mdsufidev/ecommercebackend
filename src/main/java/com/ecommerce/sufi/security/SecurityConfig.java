package com.ecommerce.sufi.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(
            JwtAuthFilter jwtAuthFilter,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler) {

        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    // =========================================================
    // PASSWORD ENCODER
    // =========================================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

            // -------------------------------------------------
            // CSRF
            // -------------------------------------------------

            .csrf(csrf -> csrf.disable())

            // -------------------------------------------------
            // DISABLE DEFAULT LOGIN
            // -------------------------------------------------

            .formLogin(form -> form.disable())

            // -------------------------------------------------
            // DISABLE HTTP BASIC
            // -------------------------------------------------

            .httpBasic(basic -> basic.disable())

            // -------------------------------------------------
            // JWT = STATELESS
            // -------------------------------------------------

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            // -------------------------------------------------
            // EXCEPTION HANDLING
            // -------------------------------------------------

            .exceptionHandling(exceptions -> exceptions

                .authenticationEntryPoint(
                    authenticationEntryPoint
                )

                .accessDeniedHandler(
                    accessDeniedHandler
                )
            )

            // -------------------------------------------------
            // AUTHORIZATION
            // -------------------------------------------------

            .authorizeHttpRequests(auth -> auth

                // =================================================
                // PUBLIC FRONTEND PAGES
                // =================================================

                .requestMatchers(
                    "/",
                    "/index.html",

                    "/login",
                    "/login.html",

                    "/register",
                    "/register.html",

                    "/forgot-password",
                    "/forgot-password.html",
                    "/reset-password",
                    "/reset-password.html",

                    "/products",
                    "/products.html",

                    "/product",
                    "/product/**",
                    "/product-detail.html",

                    "/cart",
                    "/cart.html",

                    "/checkout",
                    "/checkout.html",

                    "/addresses",
                    "/addresses.html",

                    "/profile",
                    "/profile.html",

                    "/my-orders",
                    "/my-orders.html",

                    "/order-detail",
                    "/order-detail.html",

                    "/order-success",
                    "/order-success.html"
                ).permitAll()

                // =================================================
                // ADMIN FRONTEND PAGES
                // =================================================

                .requestMatchers(
                    "/admin/**"
                ).permitAll()

                // =================================================
                // SELLER FRONTEND PAGES
                // =================================================

                .requestMatchers(
                    "/seller/**"
                ).permitAll()

                // =================================================
                // STATIC RESOURCES
                // =================================================

                .requestMatchers(
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/uploads/**",
                    "/fonts/**",
                    "/favicon.ico"
                ).permitAll()

                // =================================================
                // AUTH APIs
                // =================================================

                .requestMatchers(
                    "/api/auth/register",
                    "/api/auth/login"
                    ,"/api/auth/forgot-password"
                    ,"/api/auth/reset-password"
                    ,"/api/payments/razorpay/webhook"
                ).permitAll()

                // =================================================
                // SWAGGER / OPENAPI
                // =================================================

                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/api-docs/**"
                ).permitAll()

                // =================================================
                // ADMIN APIs
                // =================================================

                .requestMatchers(
                    "/api/admin/**"
                ).hasRole("ADMIN")

                // =================================================
                // SELLER APIs
                // =================================================

                .requestMatchers(
                    "/api/seller/**"
                ).hasAnyRole("SELLER", "ADMIN")

                // =================================================
                // CATEGORY APIs
                // =================================================

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

                // =================================================
                // PRODUCT APIs
                // =================================================

                // Public product listing/detail
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/products/**"
                ).permitAll()

                // Seller/Admin create product
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/products/**"
                ).hasAnyRole("SELLER", "ADMIN")

                // Admin approve
                .requestMatchers(
                    HttpMethod.PATCH,
                    "/api/products/*/approve"
                ).hasRole("ADMIN")

                // Admin reject
                .requestMatchers(
                    HttpMethod.PATCH,
                    "/api/products/*/reject"
                ).hasRole("ADMIN")

                // Seller/Admin update
                // Ownership check remains in ProductService
                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/products/**"
                ).hasAnyRole("SELLER", "ADMIN")

                // Seller/Admin delete
                // Ownership check remains in ProductService
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/products/**"
                ).hasAnyRole("SELLER", "ADMIN")

                // =================================================
                // EVERYTHING ELSE
                // =================================================

                .anyRequest().authenticated()
            )

            // =====================================================
            // JWT FILTER
            // =====================================================

            .addFilterBefore(
                jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
