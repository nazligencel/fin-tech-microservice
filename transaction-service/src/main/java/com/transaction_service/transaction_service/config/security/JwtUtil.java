package com.transaction_service.transaction_service.config.security;

import com.fintech.fin_tech.config.security.CustomUserDetails;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private final com.fintech.fin_tech.config.security.JwtConfig jwtConfig;

    public JwtUtil(com.fintech.fin_tech.config.security.JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    // JWT'den kullanıcı adını  çıkarır
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // JWT'den son kullanma tarihini çıkarır
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    // JWT'den tüm claim'leri çıkarır
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // İmzalama için kullanılacak gizli anahtarı alır
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtConfig.secret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Token'ın süresinin dolup dolmadığını kontrol eder
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // UserDetails nesnesinden access token üretir
    public String generateAccessToken(CustomUserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", userDetails.getId());
        claims.put("roles", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        long expirationTimeInMs = this.jwtConfig.expiration().toMillis();
        return createToken(claims, userDetails.getUsername(), expirationTimeInMs);
    }

    // Belirli claim'ler, kullanıcı adı ve geçerlilik süresi ile token oluşturur
    private String createToken(Map<String, Object> claims, String subject, long expirationTime) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject) // Token'ın konusu (genellikle kullanıcı adı)
                .setIssuedAt(new Date(System.currentTimeMillis())) // Token'ın oluşturulma zamanı
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // Token'ın son kullanma zamanı
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // İmzalama algoritması ve anahtar
                .compact();
    }

    // Token'ın geçerli olup olmadığını kontrol eder (kullanıcı adı eşleşiyor mu ve süresi dolmamış mı)
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
