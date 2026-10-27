package cl.duoc.flotaMS.repository;

import cl.duoc.flotaMS.model.Camion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface CamionRepository extends JpaRepository<Camion, Long> {
    boolean existsByPatente(String patente);

    boolean existsByPatenteAndIdCamionNot(String patente, Long id);

    List<Camion> findByEstado(String estado);

}