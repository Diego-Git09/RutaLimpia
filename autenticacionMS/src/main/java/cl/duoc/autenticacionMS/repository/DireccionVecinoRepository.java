package cl.duoc.autenticacionMS.repository;

import cl.duoc.autenticacionMS.model.DireccionVecino;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DireccionVecinoRepository
        extends JpaRepository<DireccionVecino, Long> {

    List<DireccionVecino> findByUsuario_Id(Long usuarioId);
}