package za.co.pacifish.identity_and_tenant_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import za.co.pacifish.identity_and_tenant_service.domain.Tenant;
import za.co.pacifish.identity_and_tenant_service.dto.CreateTenantRequest;
import za.co.pacifish.identity_and_tenant_service.dto.FirebaseUserDetailsDto;
import za.co.pacifish.identity_and_tenant_service.repository.TenantRepository;
import za.co.pacifish.identity_and_tenant_service.service.TenantService;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @GetMapping("/{id}")
    public ResponseEntity<Tenant> getTenant(@PathVariable String id) {
        return tenantService.getTenantById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Tenant>> getUserTenants(Authentication authentication) {
        FirebaseUserDetailsDto userDetails = (FirebaseUserDetailsDto) authentication.getPrincipal();

        if (Objects.isNull(userDetails)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(tenantService.getUserTenants(userDetails.firebaseUid()));
    }

    @PostMapping
    public ResponseEntity<Tenant> createTenant(
        @RequestBody CreateTenantRequest request, Authentication authentication) {
        FirebaseUserDetailsDto userDetails = (FirebaseUserDetailsDto) authentication.getPrincipal();
        if (Objects.isNull(userDetails)) {
            return ResponseEntity.badRequest().build();
        }
        Tenant createdTenant = tenantService.createTenant(request, userDetails.firebaseUid());
        return ResponseEntity.ok(createdTenant);
    }

}
