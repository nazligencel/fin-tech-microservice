package com.transaction_service.transaction_service.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity //Spring security web güvenliğini etkinleştirir
@EnableMethodSecurity(prePostEnabled = true) //metot seviyesi güvenlik için, mesela bir rol ekleme yetkisi varken silme yetkisi olmayabilir, silme metoduna erişimi kısıtlar
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Bu servise gelen tüm isteklerin kimliğinin doğrulanmış olmasını istiyoruz
                        .anyRequest().authenticated()
                )
                // Bu satır, Spring Security'ye gelen Bearer token'ları
                // issuer-uri'ye göre otomatik olarak doğrulamasını söyler.
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
