package com.transaction_service.transaction_service.config;
import com.transaction_service.transaction_service.config.security.JwtAuthenticationEntryPoint;
import com.transaction_service.transaction_service.config.security.JwtRequestFilter;
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

    private final JwtRequestFilter jwtRequestFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    public SecurityConfig(JwtRequestFilter jwtRequestFilter, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                //kimlik doğrulama başarısız olduğunda 401
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))//sunucu da ssession tutulmayacak
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                /**
                 * session yönetimi sunucu tarafında tutulmayacak JWT ile yönetilecek
                 * JWT filtersini spring security filtre zincirine eklenir
                 * UsernamePasswordAuthenticationFilter'den önce çalışacak
                 */
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
