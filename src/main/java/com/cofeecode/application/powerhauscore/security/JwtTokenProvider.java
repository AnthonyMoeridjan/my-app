package com.cofeecode.application.powerhauscore.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt.secret:DefaultSecretKeyNeedsToBeLongEnoughForHS256Algorithm}") // Define this in application.properties
    private String jwtSecretString;

    @Value("${app.jwt.expiration-ms:86400000}") // Define this in application.properties (default 1 day)
    private int jwtExpirationMs;

    private SecretKey jwtSecretKey;

    @jakarta.annotation.PostConstruct
    public void init() {
        byte[] secretBytes = jwtSecretString.getBytes();
        if (secretBytes.length < 32) { // HS256 needs at least 256 bits (32 bytes)
            logger.warn("JWT secret is too short (less than 32 bytes). Using a default generated key. PLEASE CONFIGURE a strong 'app.jwt.secret' in application.properties");
            this.jwtSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Generates a new secure key
        } else {
            this.jwtSecretKey = Keys.hmacShaKeyFor(secretBytes);
        }
    }

    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        // Consider fetching roles if they are not already part of UserDetails authorities as simple strings
        String roles = userPrincipal.getAuthorities().stream()
                           .map(GrantedAuthority::getAuthority)
                           .collect(Collectors.joining(","));

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim("roles", roles) // Adding roles as a claim
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(jwtSecretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateTokenFromUsername(String username, java.util.Collection<? extends GrantedAuthority> authorities) {
        String roles = authorities.stream()
                           .map(GrantedAuthority::getAuthority)
                           .collect(Collectors.joining(","));
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(jwtSecretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        } catch (io.jsonwebtoken.security.SignatureException ex) {
            logger.error("JWT signature does not match locally computed signature: {}", ex.getMessage());
        }
        return false;
    }
}
