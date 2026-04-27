//package com.automobile.ecom.config;
//
//import com.automobile.ecom.security.JwtAuthenticationFilter;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.web.cors.*;
//
//import java.util.List;
//
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity(prePostEnabled = true)
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    private final JwtAuthenticationFilter jwtFilter;
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//
//        http
//                .cors(cors -> {}) // uses corsConfigurationSource()
//                .csrf(csrf -> csrf.disable())
//
//                .sessionManagement(session ->
//                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//
//                .authorizeHttpRequests(auth -> auth
//
//                        // ✅ VERY IMPORTANT: Allow preflight requests
//                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//
//                        // ✅ Public APIs
//                        .requestMatchers("/api/auth/**").permitAll()
//                        .requestMatchers("/api/categories/**").permitAll()
//                        .requestMatchers("/api/subcategories/**").permitAll()
//                        .requestMatchers("/api/products/**").permitAll()
//                        .requestMatchers("/api/vehicles/**").permitAll()
//                        .requestMatchers("/api/compatibility/**").permitAll()
//                        .requestMatchers("/api/compatibility-details/**").permitAll()
//                        .requestMatchers("/api/search/**").permitAll()
//
//                        // ✅ Role-based APIs
//                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
//                        .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
//
//                        // ✅ Everything else secured
//                        .anyRequest().authenticated()
//                )
//
//                // ✅ JWT filter
//                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    //  FIXED CORS CONFIG (NO wildcard with credentials)
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//
//        CorsConfiguration config = new CorsConfiguration();
//
//        config.setAllowedOrigins(List.of(
//                "http://localhost:8080",
//                "http://localhost:8081",
//                "http://localhost:8082",
//                "http://127.0.0.1:3000",
//                "http://192.168.0.54:3000"
//        ));
//
//        config.setAllowedMethods(List.of(
//                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
//        ));
//
//        config.setAllowedHeaders(List.of("*"));
//
//        // ⚠️ Required for JWT / Authorization headers
//        config.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source =
//                new UrlBasedCorsConfigurationSource();
//
//        source.registerCorsConfiguration("/**", config);
//
//        return source;
//    }
//
//    // Authentication manager (needed for login)
//    @Bean
//    public AuthenticationManager authenticationManager(
//            AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }
//}
//
////

package com.automobile.ecom.config;

import com.automobile.ecom.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                                .requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers("/api/subcategories/**").permitAll()
                                .requestMatchers("/api/meta/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                                //.requestMatchers("/api/orders/**").permitAll()

                .requestMatchers("/api/auth/**",
                        "/api/categories",
                        "/api/categories/{id}",

                        // ── SubCategories ────────────────────────────────────────
                        "/api/subcategories/{id}",
                        "/api/subcategories/category/{categoryId}",

                        // ── Products ─────────────────────────────────────────────
                        "/api/products",
                        "/api/products/{id}",
                        "/api/products/subcategory/{subCategoryId}",
                        "/api/products/company/{company}",
                        "/api/products/part-number/{partNumber}",
                        "/api/products/**",

                        // ── Vehicles ─────────────────────────────────────────────
                        "/api/vehicles/{id}",
                        "/api/vehicles/brand/{brand}",
                        "/api/vehicles/brands",
                        "/api/vehicles/brands/{brand}/models",
                        "/api/vehicles/brands/{brand}/models/{model}/years",

                        // ── Vehicle Compatibility ────────────────────────────────
                        "/api/compatibility/{id}",
                        "/api/compatibility/product/{productId}",
                        "/api/compatibility/vehicle/{vehicleId}",
                        "/api/compatibility/search",

                        // ── Compatibility Filter Dropdowns ───────────────────────
                        "/api/compatibility/filter/brands",
                        "/api/compatibility/filter/fuel-types",
                        "/api/compatibility/filter/years",
                        "/api/compatibility/filter/models",
                        "/api/compatibility/filter/engines",
                        "/api/compatibility/filter/products",

                        // ── Compatibility Details ────────────────────────────────
                        "/api/compatibility/{compatibilityId}/details",
                        "/api/compatibility/details/{id}",
                        "api/search/**").permitAll()

                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of("*"));
        //config.setAllowedOrigins(List.of("http://localhost:8080")); // Or whatever your frontend port is

        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}