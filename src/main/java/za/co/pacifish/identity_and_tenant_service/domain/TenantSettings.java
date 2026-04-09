package za.co.pacifish.identity_and_tenant_service.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TenantSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String configKey;
    private String configValue;

    @JoinColumn(
        updatable = false,
        nullable = false
    )
    @ManyToOne
    private Tenant tenant;
}
