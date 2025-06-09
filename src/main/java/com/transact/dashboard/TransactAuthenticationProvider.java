import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class TransactAuthenticationProvider implements AuthenticationProvider {

    private final TransactAuthService transactAuthService;

    public TransactAuthenticationProvider(TransactAuthService transactAuthService) {
        this.transactAuthService = transactAuthService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        String status = transactAuthService.authenticateUser(username, password);

        if ("SUCCESS".equalsIgnoreCase(status)) {
            return new UsernamePasswordAuthenticationToken(
                    username, password, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        } else {
            throw new BadCredentialsException("Authentication failed: " + status);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
