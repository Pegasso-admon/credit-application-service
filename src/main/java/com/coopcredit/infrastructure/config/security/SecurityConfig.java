package com.coopcredit.infrastructure.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security configuration for the application.
 * Implements JWT-based stateless authentication with role-based authorization.
 * <p>
 * Access Control:
 * - ROLE_AFILIADO: Can view and create their own credit applications
 * - ROLE_ANALISTA: Can view pending applications and evaluate them
 * - ROLE_ADMIN: Full access to all resources
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthFilter;
        private final UserDetailsService userDetailsService;
        private final JwtAuthenticationEntryPoint authenticationEntryPoint;

        private static final String[] PUBLIC_ENDPOINTS = {
                        "/api/auth/**", // Authentication endpoints (login, register)
                        "/api/v1/auth/**", // Legacy v1 auth endpoints if any
                        "/actuator/health",
                        "/actuator/info",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
        };

        private static final String[] ADMIN_ENDPOINTS = {
                        "/api/v1/affiliates/**",
                        "/api/v1/users/**"
        };

        private static final String[] ANALYST_ENDPOINTS = {
                        "/api/v1/credit-applications/pending",
                        "/api/v1/credit-applications/*/evaluate"
        };

        /**
         * Configure CORS to allow frontend communication from localhost:5173
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:5174",
                                "http://localhost:3000"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        /**
         * Configures the security filter chain with JWT authentication.
         *
         * @param http the HttpSecurity to configure
         * @return configured SecurityFilterChain
         * @throws Exception if configuration fails
         */
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(authenticationEntryPoint))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                                                // Affiliate endpoints
                                                .requestMatchers(HttpMethod.GET, "/api/affiliates/**")
                                                .hasAnyRole("ANALYST", "ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/affiliates")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/affiliates/**")
                                                .hasRole("ADMIN")

                                                // Credit Application endpoints (both /api/ and /api/v1/)
                                                .requestMatchers(HttpMethod.POST, "/api/credit-applications",
                                                                "/api/v1/credit-applications")
                                                .hasAnyRole("AFFILIATE", "ANALYST", "ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/api/credit-applications/**",
                                                                "/api/v1/credit-applications/**")
                                                .authenticated()
                                                .requestMatchers(HttpMethod.POST, "/api/credit-applications/*/evaluate")
                                                .hasRole("ANALYST")
                                                .requestMatchers(HttpMethod.POST, "/api/credit-applications/*/approve")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/credit-applications/*/reject")
                                                .hasRole("ADMIN")

                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authenticationProvider(authenticationProvider())
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(authenticationEntryPoint));

                return http.build();
        }

        /**
         * Creates the authentication provider using UserDetailsService and
         * PasswordEncoder.
         *
         * @return configured AuthenticationProvider
         */
        @Bean
        public AuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(userDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }

        /**
         * Provides the AuthenticationManager bean.
         *
         * @param config the authentication configuration
         * @return AuthenticationManager instance
         * @throws Exception if configuration fails
         */
        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        /**
         * Provides BCrypt password encoder for secure password hashing.
         *
         * @return PasswordEncoder instance
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}