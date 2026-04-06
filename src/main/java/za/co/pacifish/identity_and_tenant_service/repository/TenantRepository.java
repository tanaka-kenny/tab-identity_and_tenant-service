package za.co.pacifish.identity_and_tenant_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.pacifish.identity_and_tenant_service.domain.Tenant;

import java.util.List;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    List<Tenant> findByOwnerFirebaseUid(String ownerFirebaseUid);
}
