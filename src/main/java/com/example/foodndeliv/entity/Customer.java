package com.example.foodndeliv.entity;

import com.example.foodndeliv.types.CustomerState;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private Boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerState state = CustomerState.ACTIVE;

    // ── Champ ABAC : lié au "sub" claim du JWT Keycloak ──
    // Rempli lors de la création du customer via l'API.
    // Utilisé par SecurityService.isOwner() pour vérifier
    // que le token appartient bien au propriétaire du compte.
    @Column(name = "keycloak_id")
    private String keycloakId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}