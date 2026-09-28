package za.co.pacifish.identity_and_tenant_service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.pacifish.identity_and_tenant_service.domain.Tenant;
import za.co.pacifish.identity_and_tenant_service.domain.TenantSettings;
import za.co.pacifish.identity_and_tenant_service.dto.TenantRequest;
import za.co.pacifish.identity_and_tenant_service.repository.TenantRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantUserService tenantUserService;

    @InjectMocks
    private TenantService tenantService;

    @Test
    void createTenant_shouldThrowWhenOwnerAlreadyHasTenant() {
        String ownerUid = "owner-uid-001";
        TenantRequest request = new TenantRequest("Acme", true, null);
        Tenant existingTenant = Tenant.builder().id(UUID.randomUUID()).userId(ownerUid).build();

        when(tenantRepository.findByUserId(ownerUid)).thenReturn(Optional.of(existingTenant));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            tenantService.createTenant(request, ownerUid));
        assertTrue(ex.getMessage().contains(ownerUid));
        verify(tenantRepository, never()).save(any(Tenant.class));
        verifyNoInteractions(tenantUserService);
    }

    @Test
    void updateTenant_shouldUseDefaultSettingsWhenRequested() {
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = Tenant.builder().id(tenantId).name("Old Name").build();
        TenantRequest request = new TenantRequest("New Name", true, null);

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(tenant)).thenReturn(tenant);

        Tenant result = tenantService.updateTenant(tenantId, request);

        assertEquals("New Name", result.getName());
        assertNotNull(result.getSettings());
        assertEquals("ZAR", result.getSettings().financialSettings().currency());
        assertTrue(result.getSettings().financialSettings().pricesIncludeTax());
    }

    @Test
    void updateTenant_shouldApplyProvidedSettingsWhenNotUsingDefault() {
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = Tenant.builder().id(tenantId).name("Old Name").build();
        TenantSettings customSettings = TenantSettings.builder()
            .financialSettings(TenantSettings.FinancialSettings.builder()
                .currency("USD")
                .defaultTipPercent(10.0)
                .taxPercent(8.0)
                .pricesIncludeTax(false)
                .build())
            .customerSettings(TenantSettings.CustomerSettings.builder()
                .waitingTimeWarningMinutes(3)
                .waitingTimeCriticalMinutes(6)
                .allowCloseWithUnpaidTabs(true)
                .qrSessionTimeoutHours(12)
                .build())
            .paymentSettings(TenantSettings.PaymentSettings.builder()
                .enabledMethods(Set.of("CARD"))
                .build())
            .build();
        TenantRequest request = new TenantRequest("Updated", false, customSettings);

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(tenant)).thenReturn(tenant);

        Tenant result = tenantService.updateTenant(tenantId, request);

        assertEquals("Updated", result.getName());
        assertEquals("USD", result.getSettings().financialSettings().currency());
        assertTrue(result.getSettings().paymentSettings().enabledMethods().contains("CARD"));
    }

    @Test
    void updateTenant_shouldThrowWhenCustomSettingsMissing() {
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = Tenant.builder().id(tenantId).name("Old Name").build();
        TenantRequest request = new TenantRequest("Updated", false, null);

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        NullPointerException ex = assertThrows(NullPointerException.class, () ->
            tenantService.updateTenant(tenantId, request));
        assertEquals("Settings cannot be null when useDefaultSettings is false", ex.getMessage());
        verify(tenantRepository, never()).save(any(Tenant.class));
    }

    @Test
    void getTenantById_shouldReturnTenantWhenFound() {
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = Tenant.builder().id(tenantId).name("Tenant A").build();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        Optional<Tenant> result = tenantService.getTenantById(tenantId.toString());

        assertTrue(result.isPresent());
        assertEquals(tenantId, result.get().getId());
    }

    @Test
    void getTenantById_shouldThrowForInvalidUuid() {
        assertThrows(IllegalArgumentException.class, () -> tenantService.getTenantById("invalid-uuid"));
    }
}
