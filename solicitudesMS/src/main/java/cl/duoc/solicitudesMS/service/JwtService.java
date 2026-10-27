package cl.duoc.solicitudesMS.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
public class JwtService {

    private final String secretBase64;

    public JwtService(@Value("${JWT_SECRET}") String secretBase64) {
        this.secretBase64 = secretBase64;
    }

    private SecretKey obtenerClave() {
        byte[] clave = Decoders.BASE64.decode(secretBase64);
        return Keys.hmacShaKeyFor(clave);
    }

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

    public Long extraerUsuarioId(String token) {
        return extraerClaims(token).get("usuarioId", Long.class);
    }

    public boolean validarToken(String token, String email) {
        try {
            return extraerClaims(token).getSubject().equals(email);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}