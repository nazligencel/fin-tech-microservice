package com.fintech.api_gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    // application.yml dosyasından issuer-uri değerini alıyoruz
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**").permitAll() // Örnek: Actuator uç noktalarına izin ver
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())); // Bu satır artık manuel olarak oluşturduğumuz bean'i kullanacak

        return http.build();
    }

    // Hatanın çözümü için bu Bean'i manuel olarak ekliyoruz
    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        // issuer-uri'yi kullanarak JWT decoder'ı oluşturuyoruz.
        // Spring geri kalanı (anahtarları indirme, cache'leme vb.) halledecektir.
        return ReactiveJwtDecoders.fromOidcIssuerLocation(issuerUri);
    }
}