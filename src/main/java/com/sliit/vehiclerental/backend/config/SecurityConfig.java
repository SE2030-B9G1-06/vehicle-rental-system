package com.sliit.vehiclerental.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import java.util.List;



@Configuration
public class SecurityConfig {



    // Password encryption
    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();

    }






    // Fix CORS for frontend + session login
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {


        CorsConfiguration configuration = new CorsConfiguration();


        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:63342",
                        "http://localhost:8080"
                )
        );


        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );


        configuration.setAllowedHeaders(
                List.of("*")
        );


        // Required because your login uses HttpSession cookies
        configuration.setAllowCredentials(true);



        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();


        source.registerCorsConfiguration(
                "/**",
                configuration
        );


        return source;

    }







    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {


        http

                .csrf(csrf -> csrf.disable())


                .cors(Customizer.withDefaults())


                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/api/health")
                        .permitAll()


                        .requestMatchers("/api/auth/**")
                        .permitAll()


                        .requestMatchers("/api/users/**")
                        .permitAll()


                        .anyRequest()
                        .permitAll()

                );



        return http.build();

    }


}