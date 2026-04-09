package za.co.pacifish.identity_and_tenant_service.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import za.co.pacifish.identity_and_tenant_service.dto.FirebaseUserDetailsDto;

import java.util.Objects;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthContextUtils {

    public static  FirebaseUserDetailsDto userDetails() {
        try {
            return (FirebaseUserDetailsDto) Objects.requireNonNull(
                SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        } catch (NullPointerException e) {
            log.error("User details not found in auth context", e);
            throw new IllegalStateException("User details not found in auth context");
        }
    }
}
