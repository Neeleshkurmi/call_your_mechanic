package com.nilesh.cym.config;

import com.nilesh.cym.token.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final boolean apiDocsEnabled;
    private final boolean swaggerUiEnabled;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            @Value("${springdoc.api-docs.enabled:false}") boolean apiDocsEnabled,
            @Value("${springdoc.swagger-ui.enabled:false}") boolean swaggerUiEnabled
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.apiDocsEnabled = apiDocsEnabled;
        this.swaggerUiEnabled = swaggerUiEnabled;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    auth.requestMatchers(
                            "/",
                            "/login",
                            "/login/",
                            "/profile/setup",
                            "/profile/setup/",
                            "/app/**",
                            "/css/**",
                            "/js/**",
                            "/images/**"
                    ).permitAll();
                    auth.requestMatchers("/api/v1/auth/**", "/swagger-ui/index.html", "/ws/**").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/v1/services/**").permitAll();

                    if (apiDocsEnabled || swaggerUiEnabled) {
                        auth.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll();
                    }

                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://10.0.2.2:*",
                "http://localhost:*",
                "http://localhost:8081",
                "http://127.0.0.1:*",
                "https://id-preview--0d0b2749-6fd4-4b84-b1df-383cc6da4bc9.lovable.app",
                "*"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
