package za.co.pacifish.identity_and_tenant_service.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.pacifish.identity_and_tenant_service.enumeration.Role;
import za.co.pacifish.identity_and_tenant_service.enumeration.TenantInvitationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TenantInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenantInvitationStatus status;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime expiration = LocalDateTime.now().plusDays(7);

    @JoinColumn(
        nullable = false,
        updatable = false
    )
    @ManyToOne(optional = false)
    private Tenant tenant;
}
