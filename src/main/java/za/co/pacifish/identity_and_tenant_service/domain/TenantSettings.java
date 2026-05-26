package za.co.pacifish.identity_and_tenant_service.domain;

import lombok.Builder;

import java.util.Set;

@Builder
public record TenantSettings(
    FinancialSettings financialSettings,
    CustomerSettings customerSettings,
    PaymentSettings paymentSettings
) {

    @Builder
    public record FinancialSettings(
        String currency,
        double defaultTipPercent,
        double taxPercent,
        boolean pricesIncludeTax
    ) {
    }

    @Builder
    public record CustomerSettings(
        int waitingTimeWarningMinutes,
        int waitingTimeCriticalMinutes,
        boolean allowCloseWithUnpaidTabs,
        int qrSessionTimeoutHours
    ) {
    }

    @Builder
    public record PaymentSettings(
        Set<String> enabledMethods
    ) {
    }
}





