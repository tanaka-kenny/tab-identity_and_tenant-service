package za.co.pacifish.identity_and_tenant_service.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import za.co.pacifish.identity_and_tenant_service.enumeration.TenantStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TenantStatus status;

    @Column(nullable = false)
    private String ownerFirebaseUid;

    @CreationTimestamp
    private LocalDateTime createdAt;

}
