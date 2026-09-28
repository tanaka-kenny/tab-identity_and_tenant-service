package za.co.pacifish.identity_and_tenant_service.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.pacifish.identity_and_tenant_service.enumeration.Role;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
    indexes = {
        @Index(name = "idx_tenant_user_email", columnList = "email")
    }
)
public class TenantUser {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
        nullable = false
    )
    private String userId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @JoinColumn(
        nullable = false,
        updatable = false
    )
    @ManyToOne(optional = false)
    private Tenant tenant;
}