package za.co.pacifish.identity_and_tenant_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.pacifish.identity_and_tenant_service.domain.TenantInvitation;

import java.util.UUID;

public interface TenantInvitationRepository extends JpaRepository<TenantInvitation, UUID> {
}
