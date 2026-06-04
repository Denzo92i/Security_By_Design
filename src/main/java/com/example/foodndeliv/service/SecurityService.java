package com.example.foodndeliv.service;

import com.example.foodndeliv.entity.Customer;
import com.example.foodndeliv.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/**
 * Service ABAC (Attribute-Based Access Control).
 *
 * Utilisé dans les @PreAuthorize via @securityService.isOwner(...)
 * pour vérifier qu'un utilisateur authentifié agit sur son propre compte.
 *
 * Le JWT Keycloak contient un claim "sub" (subject) qui est l'ID unique
 * de l'utilisateur dans Keycloak. On le compare au keycloakId stocké
 * dans l'entité Customer.
 */
@Service("securityService")
public class SecurityService {

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Vérifie que l'utilisateur authentifié est bien le propriétaire
     * du compte customer identifié par customerId.
     *
     * @param customerId     L'ID du customer en base
     * @param authentication Le contexte de sécurité Spring (contient le JWT)
     * @return true si le sub du JWT correspond au keycloakId du customer
     */
    public boolean isOwner(Long customerId, Authentication authentication) {
        if (authentication == null || customerId == null) {
            return false;
        }

        // Extraire le "sub" claim du JWT Keycloak
        String jwtSubject;
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            jwtSubject = jwt.getSubject();
        } else {
            // Fallback : utiliser getName() (fonctionnel avec most JWT configs)
            jwtSubject = authentication.getName();
        }

        if (jwtSubject == null || jwtSubject.isBlank()) {
            return false;
        }

        // Chercher le customer et comparer son keycloakId avec le sub du token
        return customerRepository.findById(customerId)
                .map(customer -> jwtSubject.equals(customer.getKeycloakId()))
                .orElse(false);
    }
}