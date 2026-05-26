package za.co.pacifish.identity_and_tenant_service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.pacifish.identity_and_tenant_service.domain.Tenant;
import za.co.pacifish.identity_and_tenant_service.dto.TenantRequest;
import za.co.pacifish.identity_and_tenant_service.repository.TenantRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantService tenantService;

    @Test
    void createTenant_shouldSaveTenantWithCorrectFields() {
        String tenantName = "Acme Corp";
        String ownerUid = "firebase-uid-123";
        TenantRequest request = new TenantRequest(tenantName);

        UUID generatedId = UUID.randomUUID();
        Tenant savedTenant = Tenant.builder()
            .id(generatedId)
            .name(tenantName)
            .ownerFirebaseUid(ownerUid)
            .build();

        when(tenantRepository.save(any(Tenant.class))).thenReturn(savedTenant);

        Tenant result = tenantService.createTenant(request, ownerUid);

        assertNotNull(result);
        assertEquals(generatedId, result.getId());
        assertEquals(tenantName, result.getName());
        assertEquals(ownerUid, result.getOwnerFirebaseUid());

        ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
        verify(tenantRepository).save(captor.capture());
        Tenant captured = captor.getValue();
        assertEquals(tenantName, captured.getName());
        assertEquals(ownerUid, captured.getOwnerFirebaseUid());
    }

    @Test
    void createTenant_shouldCallRepositorySaveOnce() {
        TenantRequest request = new TenantRequest("Test Tenant");
        when(tenantRepository.save(any(Tenant.class))).thenReturn(Tenant.builder().build());

        tenantService.createTenant(request, "uid-456");

        verify(tenantRepository, times(1)).save(any(Tenant.class));
    }

    @Test
    void getTenantById_shouldReturnTenantWhenFound() {
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = Tenant.builder()
            .id(tenantId)
            .name("Found Tenant")
            .ownerFirebaseUid("uid-789")
            .build();

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        Optional<Tenant> result = tenantService.getTenantById(tenantId.toString());

        assertTrue(result.isPresent());
        assertEquals(tenantId, result.get().getId());
        assertEquals("Found Tenant", result.get().getName());
        verify(tenantRepository).findById(tenantId);
    }

    @Test
    void getTenantById_shouldReturnEmptyWhenNotFound() {
        UUID tenantId = UUID.randomUUID();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        Optional<Tenant> result = tenantService.getTenantById(tenantId.toString());

        assertTrue(result.isEmpty());
        verify(tenantRepository).findById(tenantId);
    }

    @Test
    void getTenantById_shouldThrowForInvalidUuid() {
        assertThrows(IllegalArgumentException.class, () ->
            tenantService.getTenantById("not-a-valid-uuid"));
    }

    @Test
    void getUserTenants_shouldReturnTenantsForOwner() {
        String ownerUid = "owner-uid-001";
        List<Tenant> tenants = List.of(
            Tenant.builder().id(UUID.randomUUID()).name("Tenant A").ownerFirebaseUid(ownerUid).build(),
            Tenant.builder().id(UUID.randomUUID()).name("Tenant B").ownerFirebaseUid(ownerUid).build()
        );

        when(tenantRepository.findByOwnerFirebaseUid(ownerUid)).thenReturn(tenants);

        List<Tenant> result = tenantService.getUserTenants(ownerUid);

        assertEquals(2, result.size());
        assertEquals("Tenant A", result.get(0).getName());
        assertEquals("Tenant B", result.get(1).getName());
        verify(tenantRepository).findByOwnerFirebaseUid(ownerUid);
    }

    @Test
    void getUserTenants_shouldReturnEmptyListWhenNoTenants() {
        String ownerUid = "owner-uid-002";
        when(tenantRepository.findByOwnerFirebaseUid(ownerUid)).thenReturn(Collections.emptyList());

        List<Tenant> result = tenantService.getUserTenants(ownerUid);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(tenantRepository).findByOwnerFirebaseUid(ownerUid);
    }
}