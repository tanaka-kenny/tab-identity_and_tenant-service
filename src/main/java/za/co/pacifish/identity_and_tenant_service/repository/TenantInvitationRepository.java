package za.co.pacifish.identity_and_tenant_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.pacifish.identity_and_tenant_service.domain.TenantInvitation;
import za.co.pacifish.identity_and_tenant_service.enumeration.TenantInvitationStatus;

import java.util.Optional;
import java.util.UUID;

public interface TenantInvitationRepository extends JpaRepository<TenantInvitation, UUID> {
    Optional<TenantInvitation> findByTokenHash(String tokenHash);

    boolean existsByEmailIgnoreCaseAndTenant_IdAndStatus(String email, UUID tenantId, TenantInvitationStatus status);
}
