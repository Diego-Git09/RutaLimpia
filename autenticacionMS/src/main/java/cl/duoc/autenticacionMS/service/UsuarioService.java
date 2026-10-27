package cl.duoc.autenticacionMS.service;

import cl.duoc.autenticacionMS.dto.CrearVecinoDTO;
import cl.duoc.autenticacionMS.dto.UsuarioCreadoDTO;
import cl.duoc.autenticacionMS.model.Rol;
import cl.duoc.autenticacionMS.model.Usuario;
import cl.duoc.autenticacionMS.repository.RolRepository;
import cl.duoc.autenticacionMS.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioCreadoDTO registrarVecino(CrearVecinoDTO datos) {
        if (usuarioRepository.existsByEmail(datos.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un usuario con ese correo"
            );
        }

        if (usuarioRepository.existsByRut(datos.getRut())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un usuario con ese RUT"
            );
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(datos.getNombre());
        usuario.setApellido(datos.getApellido());
        usuario.setRut(datos.getRut());
        usuario.setEmail(datos.getEmail());
        usuario.setTelefono(datos.getTelefono());
        usuario.setPasswordHash(
                passwordEncoder.encode(datos.getContrasena())
        );

        Rol rolVecino = rolRepository.findByNombre("VECINO")
                .orElseThrow(() -> new IllegalStateException(
                        "El rol VECINO no está cargado. Revisa data.sql"
                ));

        usuario.setRol(rolVecino);

        Usuario guardado = usuarioRepository.save(usuario);

        return new UsuarioCreadoDTO(
                guardado.getId(),
                guardado.getNombre(),
                guardado.getEmail(),
                guardado.getRol().getNombre()
        );
    }

    @Transactional(readOnly = true)
    public UsuarioCreadoDTO consultarPerfil(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un usuario con ese correo"
                ));

        return new UsuarioCreadoDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol().getNombre()
        );
    }
}