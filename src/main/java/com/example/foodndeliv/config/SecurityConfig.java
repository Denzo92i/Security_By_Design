package com.example.foodndeliv.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll() 
                
                // On utilise hasAnyAuthority pour couvrir les deux formats de rôle possibles
                .requestMatchers(HttpMethod.POST, "/api/ctrl/customers").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/ctrl/customers").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/ctrl/customers").hasAnyAuthority("ADMIN", "ROLE_ADMIN", "USER", "ROLE_USER")
                
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );
        
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess == null || !realmAccess.containsKey("roles")) {
                return Collections.emptyList();
            }
            
            Collection<String> roles = (Collection<String>) realmAccess.get("roles");
            
            // Log de débogage pour voir exactement ce que Spring reçoit
            System.out.println("DEBUG - Rôles extraits du jeton : " + roles);
            
            return roles.stream()
                .map(role -> {
                    // On ajoute "ROLE_" manuellement pour être compatible avec hasAnyAuthority
                    String roleName = role.toUpperCase();
                    return roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
                })
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        });
        
        return converter;
    }
}