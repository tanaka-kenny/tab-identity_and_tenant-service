package za.co.pacifish.identity_and_tenant_service.filter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;
import za.co.pacifish.identity_and_tenant_service.dto.FirebaseUserDetailsDto;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FirebaseIdTokenFilter extends OncePerRequestFilter {
    private final JsonMapper objectMapper;
    private final FirebaseAuth firebaseAuth;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.replace("Bearer ", "");
        try {
            Optional<FirebaseUserDetailsDto> firebaseUserDetails = extractUserDetailsFromToken(token);
            if (firebaseUserDetails.isPresent()) {
                UsernamePasswordAuthenticationToken authenticationToken
                    = new UsernamePasswordAuthenticationToken(firebaseUserDetails.get(), null, null);
                authenticationToken.setDetails(new WebAuthenticationDetails(request));

                SecurityContext newContext = SecurityContextHolder.createEmptyContext();
                newContext.setAuthentication(authenticationToken);
                SecurityContextHolder.setContext(newContext);
            } else {
                setAuthErrorDetails(response);
            }
        } catch (FirebaseAuthException _) {
            setAuthErrorDetails(response);
        } finally {
            filterChain.doFilter(request, response);
        }

    }

    private Optional<FirebaseUserDetailsDto> extractUserDetailsFromToken(String token) throws FirebaseAuthException {
        FirebaseToken firebaseToken = firebaseAuth.verifyIdToken(token);
        String userId = String.valueOf(firebaseToken.getClaims().get("user_id"));
        String email = firebaseToken.getEmail();

        FirebaseUserDetailsDto firebaseUserDetails = new FirebaseUserDetailsDto(email, userId);
        return Optional.of(firebaseUserDetails);
    }

    private void setAuthErrorDetails(HttpServletResponse response) throws IOException {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            status, "Authentication failure: Token missing, invalid or expired");
        response.getWriter().write(objectMapper.writeValueAsString(problemDetail));
    }
}
