package fec.digiflow.common.security;

import fec.digiflow.common.exception.ApplicationException;
import fec.digiflow.common.message.GlobalMessage;
import fec.partner.common.dtos.SessionUser;
import fec.partner.common.enums.CommonMessage;
import fec.partner.common.exceptions.CommonException;
import fec.partner.common.utils.JacksonUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import static fec.digiflow.common.consts.GlobalApplicationConst.API_KEY_HEADER;
import static fec.digiflow.common.consts.GlobalApplicationConst.USER_KEY_HEADER;
import static fec.partner.common.consts.CommonApplicationConst.API_KEY_HEADER;
import static fec.partner.common.consts.CommonApplicationConst.USER_KEY_HEADER;

@Slf4j
public class AuthenticateHeaderFilter extends OncePerRequestFilter {

    private final String SERVICE_KEY;
    private final String[] WHITELISTED_URLS;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AuthenticateHeaderFilter(String serviceKey, String[] whitelistedUrls) {
        SERVICE_KEY = serviceKey;
        this.WHITELISTED_URLS = whitelistedUrls;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI().substring(request.getContextPath().length());
        for (String pattern : WHITELISTED_URLS) {
            if (pathMatcher.match(pattern, path)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        String apiKey = request.getHeader(API_KEY_HEADER);
        if (!StringUtils.hasText(apiKey) || !apiKey.equals(SERVICE_KEY)) {
            throw new ApplicationException(GlobalMessage.UNAUTHORIZED);
        }


        String userHeaderValue = request.getHeader(USER_KEY_HEADER);
        if (userHeaderValue == null || userHeaderValue.isEmpty() || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            Authentication authentication = decodeHeaderToSessionUser(userHeaderValue);
            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                SecurityContextHolder.clearContext();
            }
        } catch (Exception e) {
            log.error("Failed to process x-user header. Clearing security context.", e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private Authentication decodeHeaderToSessionUser(String headerValue) {
        String decodedJson;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(headerValue);
            decodedJson = new String(decodedBytes);
        } catch (IllegalArgumentException e) {
            log.debug("x-user header is not valid Base64. Value: '{}'", headerValue);
            return null;
        }

        if (!decodedJson.trim().startsWith("{")) {
            log.debug("Decoded x-user header is not a JSON object: '{}'", decodedJson);
            return null;
        }

        return null;
    }
}
