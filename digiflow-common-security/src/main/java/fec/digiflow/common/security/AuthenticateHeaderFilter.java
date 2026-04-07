package fec.digiflow.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class AuthenticateHeaderFilter extends OncePerRequestFilter {

    private final String serviceKey;
    private final String[] whitelistedUrls;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AuthenticateHeaderFilter(String serviceKey, String[] whitelistedUrls) {
        this.serviceKey = serviceKey;
        this.whitelistedUrls = whitelistedUrls;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Check if path is whitelisted
        String requestURI = request.getRequestURI();
        for (String whitelistedUrl : whitelistedUrls) {
            if (pathMatcher.match(whitelistedUrl, requestURI)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // Validate Service Key or any custom Authentication header
        String reqServiceKey = request.getHeader("X-Service-Key");

        if (StringUtils.hasText(serviceKey) && serviceKey.equals(reqServiceKey)) {
            // Fake an authentication context for Service-to-Service communication
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_SERVICE"));
            User principal = new User("service_account", "", authorities);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}