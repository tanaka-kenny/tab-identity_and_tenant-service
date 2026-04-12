package za.co.pacifish.identity_and_tenant_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import za.co.pacifish.identity_and_tenant_service.domain.Tenant;
import za.co.pacifish.identity_and_tenant_service.dto.CreateTenantRequest;
import za.co.pacifish.identity_and_tenant_service.dto.FirebaseUserDetailsDto;
import za.co.pacifish.identity_and_tenant_service.service.TenantService;

import java.util.List;

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
    public ResponseEntity<List<Tenant>> getUserTenants(
        @AuthenticationPrincipal FirebaseUserDetailsDto userDetails) {
        return ResponseEntity.ok(tenantService.getUserTenants(userDetails.firebaseUid()));
    }

    @PostMapping
    public ResponseEntity<Tenant> createTenant(
        @Valid @RequestBody CreateTenantRequest request,
        @AuthenticationPrincipal FirebaseUserDetailsDto userDetails) {
        Tenant createdTenant = tenantService.createTenant(request, userDetails.firebaseUid());
        return ResponseEntity.ok(createdTenant);
    }

}
