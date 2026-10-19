package pe.edu.cibertec.cybercomputer.inventory.security;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/internal/stock/descontar",
                                "/api/v1/internal/stock/reponer").hasRole("ORDER_SERVICE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/categorias", "/api/v1/productos",
                                "/api/v1/productos/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/productos", "/api/v1/categorias").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/productos/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/productos/*").hasRole("ADMIN")
                        .anyRequest().denyAll())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((req, res, ex) -> SecurityErrors.unauthorized(res))
                        .accessDeniedHandler((req, res, ex) -> SecurityErrors.forbidden(res)))
                .addFilterBefore(new FiltroJwtAuth(jwtService), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
