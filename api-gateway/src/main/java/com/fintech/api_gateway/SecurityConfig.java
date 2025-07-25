package com.fintech.api_gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                // CORS ayarlarını kullan (WebFlux için ayrı bir CORS yapılandırması gerekir,
                // veya application.yml'de global olarak ayarlanabilir)
                .cors(Customizer.withDefaults())

                // CSRF koruması devre dışı
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // Yetkilendirme kuralları
                .authorizeExchange(exchange -> exchange
                        // ÖNEMLİ: Frontend'in Keycloak'a yönlendirilip geri döneceği
                        // veya login/logout ile ilgili path'leriniz varsa, bunlara izin vermeniz gerekebilir.
                        // Şimdilik, tüm API yollarını koruyalım.

                        // Eureka Server ile iletişim için Gateway'in kendi path'lerine izin ver (eğer Gateway de bir client ise)
                        .pathMatchers("/eureka/**").permitAll()

                        // Transaction servisine giden tüm yolların kimliğinin doğrulanmış olmasını ve
                        // "USER" rolüne sahip olmasını zorunlu kıl.
                        .pathMatchers("/api/transactions/**").hasRole("USER") // 'USER' rolünü gerektirir

                        // Diğer tüm isteklerin de kimliğinin doğrulanmış olmasını isteyelim
                        .anyExchange().authenticated()
                )

                // Gelen JWT'leri issuer-uri'ye göre otomatik olarak doğrulaması için
                // OAuth2 Resource Server desteğini aktif et.
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                );

        return http.build();
    }
}