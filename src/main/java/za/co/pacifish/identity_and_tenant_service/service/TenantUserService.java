package za.co.pacifish.identity_and_tenant_service.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import za.co.pacifish.identity_and_tenant_service.domain.Tenant;
import za.co.pacifish.identity_and_tenant_service.domain.TenantUser;
import za.co.pacifish.identity_and_tenant_service.dto.CreateTenantUserRequest;
import za.co.pacifish.identity_and_tenant_service.exception.ExternalServiceException;
import za.co.pacifish.identity_and_tenant_service.mapper.TenantUserMapper;
import za.co.pacifish.identity_and_tenant_service.repository.TenantRepository;
import za.co.pacifish.identity_and_tenant_service.repository.TenantUserRepository;
import za.co.pacifish.identity_and_tenant_service.utils.AuthContextUtils;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantUserService {

    private final TenantUserRepository tenantUserRepository;
    private final TenantRepository tenantRepository;
    private final FirebaseAuth firebaseAuth;

    public Optional<TenantUser> createTenantUser(CreateTenantUserRequest request) {
        String tenantId = AuthContextUtils.userDetails().tenantId();
        return tenantRepository.findById(UUID.fromString(tenantId)).map(
            tenant -> createTenantUser(request, tenant)
        );
    }

    public TenantUser createTenantUser(CreateTenantUserRequest request, Tenant tenant) {
        TenantUser tenantUser = TenantUserMapper.toEntity(request, tenant);
        log.info("Creating new tenant user for tenant: {}", tenant.getId());
        tenantUser = tenantUserRepository.save(tenantUser);

        Map<String, Object> claims = new HashMap<>();
        claims.put("tenantId", tenant.getId().toString());
        claims.put("roles", List.of(request.role().name()));

        try {
            firebaseAuth.setCustomUserClaims(request.firebaseUid(), claims);
        } catch (FirebaseAuthException ex) {
            log.error("Failed to update Firebase user: {} Auth claims: {}", request.firebaseUid(), ex.getMessage());
            throw new ExternalServiceException("An internal error occurred while trying to create tenant user. Please try again later");
        }

        return tenantUser;
    }

    public List<TenantUser> findAllByTenantId() {
        String tenantId = AuthContextUtils.userDetails().tenantId();
        log.info("Finding all tenant users for tenant: {}", tenantId);
        return tenantUserRepository.findAllByTenant_Id(UUID.fromString(tenantId));
    }


}
