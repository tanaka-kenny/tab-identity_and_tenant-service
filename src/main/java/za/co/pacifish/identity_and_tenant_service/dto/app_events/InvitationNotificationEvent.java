package za.co.pacifish.identity_and_tenant_service.dto.app_events;

import java.util.UUID;

public record InvitationNotificationEvent(
    String recipient,
    UUID invitationId,
    String tenantName,
    String inviterName
    ) {}