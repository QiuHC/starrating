package com.starrating.security;

import com.starrating.dto.AuthenticatedUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtils {

    @Value("${app.jwt.secret:starrating_secret_key_fixed_length_string_for_demo}")
    private String secret;

    @Value("${app.jwt.expiration-ms:3600000}")
    private int expiration;

    public String generateToken(AuthenticatedUser authenticatedUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", authenticatedUser.getUser().getId());
        claims.put("userType", authenticatedUser.getUser().getUserType());
        claims.put("shopCode", authenticatedUser.getUser().getShopCode());
        claims.put("roleCodes", authenticatedUser.getRoleCodes());
        claims.put("permissionCodes", authenticatedUser.getPermissionCodes());
        return createToken(claims, authenticatedUser.getUser().getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    @SuppressWarnings("unchecked")
    public SecurityPrincipal extractPrincipal(String token) {
        Claims claims = extractAllClaims(token);
        SecurityPrincipal principal = new SecurityPrincipal();
        principal.setUserId(((Number) claims.get("userId")).longValue());
        principal.setUsername(claims.getSubject());
        principal.setUserType((String) claims.get("userType"));
        principal.setShopCode((String) claims.get("shopCode"));
        principal.setRoleCodes((List<String>) claims.get("roleCodes"));
        principal.setPermissionCodes((List<String>) claims.get("permissionCodes"));
        return principal;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
