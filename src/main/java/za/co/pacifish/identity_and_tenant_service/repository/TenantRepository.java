package za.co.pacifish.identity_and_tenant_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.pacifish.identity_and_tenant_service.domain.Tenant;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByOwnerFirebaseUid(String ownerFirebaseUid);
}
