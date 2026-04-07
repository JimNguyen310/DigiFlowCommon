package fec.digiflow.common.security.jwt;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final String AUTHORITIES_KEY = "auth";

    private PublicKey publicKey;

    public JwtTokenProvider(
            @Value("${security.jwt.public-key:}") String publicKeyStr) {
        
        if (publicKeyStr != null && !publicKeyStr.isEmpty()) {
            this.publicKey = parsePublicKey(publicKeyStr);
        } else {
            log.warn("JWT Public Key is not configured. Token validation will fail.");
        }
    }

    private PublicKey parsePublicKey(String keyStr) {
        try {
            String key = keyStr
                    .replaceAll("-----BEGIN PUBLIC KEY-----", "")
                    .replaceAll("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] keyBytes = Base64.getDecoder().decode(key);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
        } catch (Exception e) {
            log.error("Failed to parse Public Key for JWT validation", e);
            return null;
        }
    }

    public Authentication getAuthentication(String token) {
        if (publicKey == null) {
            throw new IllegalStateException("Cannot validate JWT: Public key not configured");
        }

        Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Extract authorities (handles IAM's RS256 token format)
        Object authObj = claims.get("authorities");
        Collection<? extends GrantedAuthority> authorities = null;

        if (authObj instanceof Collection) {
            authorities = ((Collection<?>) authObj).stream()
                    .map(Object::toString)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        } else if (authObj != null) {
            authorities = Arrays.stream(authObj.toString().split(","))
                    .filter(auth -> !auth.trim().isEmpty())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        } else {
            authorities = java.util.Collections.emptyList();
        }

        User principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String authToken) {
        if (publicKey == null) {
            return false;
        }
        try {
            Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.info("Invalid JWT token: {}", e.getMessage());
            log.trace("Invalid JWT token trace.", e);
        }
        return false;
    }
}
