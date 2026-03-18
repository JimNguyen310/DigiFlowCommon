package fec.digiflow.common.security;

import fec.digiflow.common.dto.SessionUser;
import fec.digiflow.common.exception.CommonException;
import fec.digiflow.common.message.GlobalMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

public class BaseAuthenticateComponent {
    private static final Logger log = LoggerFactory.getLogger(BaseAuthenticateComponent.class);

    public String getUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                SessionUser userInfo = (SessionUser) authentication.getPrincipal();
                if (userInfo != null) {
                    return userInfo.username();
                }
                return null;
            }
        } catch (Exception e) {
            throw new CommonException(GlobalMessage.UNAUTHORIZED);
        }
        return null;
    }

    public String getUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                SessionUser userInfo = (SessionUser) authentication.getPrincipal();
                if (userInfo != null) {
                    return userInfo.id();
                }
                return null;
            }
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }
        return null;
    }

    public List<String> getAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        }
        return List.of();
    }
}
