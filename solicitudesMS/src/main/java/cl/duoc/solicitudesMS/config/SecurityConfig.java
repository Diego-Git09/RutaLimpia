package cl.duoc.solicitudesMS.config;

import cl.duoc.solicitudesMS.service.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService jwtService
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint((request, response, exception) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/solicitudes"
                        ).hasRole("VECINO")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/solicitudes/**"
                        ).hasRole("VECINO")
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/solicitudes/*/resultado"
                        ).hasRole("CONDUCTOR")
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/solicitudes/*/asignacion"
                        ).hasRole("CONDUCTOR")
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtService),
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }
}