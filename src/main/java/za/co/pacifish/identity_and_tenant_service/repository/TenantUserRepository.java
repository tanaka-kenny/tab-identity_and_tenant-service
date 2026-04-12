package za.co.pacifish.identity_and_tenant_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.pacifish.identity_and_tenant_service.domain.TenantUser;

import java.util.List;
import java.util.UUID;

public interface TenantUserRepository extends JpaRepository<TenantUser, UUID> {

    List<TenantUser> findAllByTenant_Id(UUID tenantId);
}
