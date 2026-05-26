package za.co.pacifish.identity_and_tenant_service.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import za.co.pacifish.identity_and_tenant_service.domain.Tenant;
import za.co.pacifish.identity_and_tenant_service.domain.TenantSettings;
import za.co.pacifish.identity_and_tenant_service.dto.TenantRequest;
import za.co.pacifish.identity_and_tenant_service.enumeration.TenantStatus;

import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TenantMapper {

    public static Tenant ofDefaultSettings(TenantRequest request, String ownerFirebaseUid) {
        return Tenant.builder()
            .name(request.name())
            .ownerFirebaseUid(ownerFirebaseUid)
            .status(TenantStatus.ACTIVE)
            .settings(defaultSettings())
            .build();
    }

    public static TenantSettings defaultSettings() {

        TenantSettings.FinancialSettings financialSettings = TenantSettings.FinancialSettings.builder()
            .currency("ZAR")
            .defaultTipPercent(15.0)
            .taxPercent(15.0)
            .pricesIncludeTax(true)
            .build();

        TenantSettings.CustomerSettings customerSettings = TenantSettings.CustomerSettings.builder()
            .waitingTimeCriticalMinutes(5)
            .waitingTimeCriticalMinutes(10)
            .allowCloseWithUnpaidTabs(false)
            .qrSessionTimeoutHours(24)
            .build();

        TenantSettings.PaymentSettings paymentSettings = TenantSettings.PaymentSettings.builder()
            .enabledMethods(Set.of("ALL"))
            .build();

        return new TenantSettings(financialSettings, customerSettings, paymentSettings);
    }
}
