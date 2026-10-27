package cl.duoc.rutasMS.repository;

import cl.duoc.rutasMS.model.Parada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParadaRepository extends JpaRepository<Parada, Long> {

    List<Parada> findByRuta_IdRutaOrderByOrdenAsc(Long idRuta);

    Optional<Parada> findByIdParadaAndRuta_IdRuta(
            Long idParada, Long idRuta);

    boolean existsByFolio(String folio);

    boolean existsByRuta_IdRutaAndOrden(Long idRuta, Integer orden);

    boolean existsByRuta_IdRuta(Long idRuta);
}