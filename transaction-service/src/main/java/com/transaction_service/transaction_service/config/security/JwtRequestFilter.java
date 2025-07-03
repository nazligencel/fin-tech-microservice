package com.transaction_service.transaction_service.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *  Bu sınıfın örevi gelen isteğin Authorization header'ını okumak, JWT'yi çıkarmak ve JwtUtil ile doğrulamaktır.
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    // CONSTRUCTOR GÜNCELLENDİ: Artık UserDetailsService almıyor.
    @Autowired
    public JwtRequestFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
             HttpServletRequest request,
             HttpServletResponse response,
             FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Eğer Authorization header'ı yoksa veya "Bearer " ile başlamıyorsa,
        // bu filtreyi pas geçip zincirdeki bir sonraki filtreye devam et.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7); // "Bearer " kısmını atla (7 karakter)

        // Eğer SecurityContext'te zaten bir kimlik doğrulaması varsa, tekrar yapmaya gerek yok.
        // Bu, bazı zincirleme filtre durumlarında performansı artırabilir.
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Token'ı doğrula (imza ve son kullanma tarihi)
            if (jwtUtil.validateToken(jwt)) {
                // Token geçerliyse, içindeki claim'leri çıkar.
                Claims claims = jwtUtil.extractAllClaims(jwt);

                // "user_id" claim'ini Long olarak al.
                Integer userIdInt = claims.get("user_id", Integer.class); // auth-service'te hangi isimle ise
                if (userIdInt == null) {
                    throw new IllegalArgumentException("JWT token does not contain 'user_id' claim.");
                }
                Long userId = userIdInt.longValue();

                // "roles" claim'ini alıp GrantedAuthority listesine çevir.
                List<String> roles = claims.get("roles", List.class);
                Collection<? extends GrantedAuthority> authorities =
                        (roles == null || roles.isEmpty())
                                ? Collections.emptyList()
                                : roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

                // Spring Security için bir Authentication nesnesi oluştur.
                // Principal olarak UserDetails nesnesi yerine doğrudan userId (Long) kullanıyoruz.
                // Credentials (şifre) null, çünkü token ile kimlik doğruluyoruz.
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userId,       // Principal: Artık Long tipinde kullanıcı ID'si
                        null,         // Credentials
                        authorities   // Yetkiler
                );

                // Authentication nesnesine isteğin detaylarını (IP adresi, session ID vb.) ekle.
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Oluşturulan Authentication nesnesini SecurityContext'e yerleştir.
                // Bu noktadan sonra, uygulama bu isteği kimliği doğrulanmış olarak kabul eder.
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (ExpiredJwtException e) {
            logger.warn("JWT Token has expired: {}");
            // Yanıtı burada kesip 401 göndermek de bir seçenek, ama genellikle
            // JwtAuthenticationEntryPoint bu işi daha merkezi yapar.
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}");
            // Beklenmedik bir hata durumunda context'i temizlemek güvenlidir.
            SecurityContextHolder.clearContext();
        }

        // Zincirdeki bir sonraki filtreye devam et.
        filterChain.doFilter(request, response);
    }
}
