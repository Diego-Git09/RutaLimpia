package cl.duoc.autenticacionMS.config;

import cl.duoc.autenticacionMS.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Usamos JwtService para leer y validar los datos del token.
    private final JwtService jwtService;

    // Spring entrega JwtService automáticamente al crear este filtro.
    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    // Este método se ejecuta antes de que la petición llegue al controlador.
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Leemos el encabezado Authorization de la petición HTTP.
        // Leemos el encabezado HTTP que Postman envía con la petición.
        // Ejemplo: "Authorization: Bearer eyJhbGciOi..."
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Revisamos que exista el encabezado y que comience con "Bearer ".
        // Si no trae token, dejamos que Spring Security decida si la ruta es pública
        // o si requiere autenticación.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Quitamos "Bearer " (son 7 caracteres).
        // Nos quedamos solo con el token: "eyJhbGciOi..."
        String token = authHeader.substring(7);
        try {
            // Leemos el correo y el rol incluidos en el token.
            // JwtService también comprueba la firma y el vencimiento.
            String email = jwtService.extraerEmail(token);
            String rol = jwtService.extraerRol(token);

            // Solo creamos la autenticación si el token es válido
            // y Spring todavía no autenticó esta petición.
            if (jwtService.validarToken(token, email)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Spring usa este objeto para identificar al usuario.
                // "ROLE_" permite que las reglas de roles lo reconozcan.
                UsernamePasswordAuthenticationToken autenticacion =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + rol))
                        );

                // Guardamos la identidad y el rol para esta petición.
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            }
        } catch (JwtException | IllegalArgumentException e) {
            // Si no se puede leer o validar el token, rechazamos la petición.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Continuamos con los filtros restantes y luego con el controlador.
        filterChain.doFilter(request, response);
    }
}