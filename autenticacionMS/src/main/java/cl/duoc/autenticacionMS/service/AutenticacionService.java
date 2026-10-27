package cl.duoc.autenticacionMS.service;

import cl.duoc.autenticacionMS.dto.LoginDTO;
import cl.duoc.autenticacionMS.dto.RespuestaLoginDTO;
import cl.duoc.autenticacionMS.model.Usuario;
import cl.duoc.autenticacionMS.repository.UsuarioRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AutenticacionService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AutenticacionService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public RespuestaLoginDTO iniciarSesion(LoginDTO datos) {
        Usuario usuario = usuarioRepository.findByEmail(datos.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Correo o contraseña incorrectos" // A proposito por seguridad.
                ));

        boolean contrasenaCorrecta = passwordEncoder.matches(
                datos.getContrasena(),
                usuario.getPasswordHash()
        );

        if (!contrasenaCorrecta) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Correo o contraseña incorrectos"
            );
        }

        String token = jwtService.generarToken(
                usuario.getEmail(),
                usuario.getRol().getNombre()
        );

        return new RespuestaLoginDTO(token, "Bearer");
    }
}