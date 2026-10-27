package cl.duoc.autenticacionMS.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final String secretBase64;
    private final long tiempoExpiracion = 60 * 60 * 1000;

    public JwtService(@Value("${JWT_SECRET}") String secretBase64) {
        this.secretBase64 = secretBase64;
    }

    private SecretKey obtenerClave() {
        byte[] clave = Decoders.BASE64.decode(secretBase64);
        return Keys.hmacShaKeyFor(clave);
    }

    public String generarToken(String email, String rol) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + tiempoExpiracion);

        return Jwts.builder()
                .subject(email)
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(obtenerClave())
                .compact();
    }

    public boolean validarToken(String token, String email) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(obtenerClave())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject().equals(email);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // extraerClaims primero verifica la firma y después permite leer los datos.
    private Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(obtenerClave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extraerEmail(String token) {
        return extraerClaims(token).getSubject();
    }

    public String extraerRol(String token) {
        return extraerClaims(token).get("rol", String.class);
    }
}