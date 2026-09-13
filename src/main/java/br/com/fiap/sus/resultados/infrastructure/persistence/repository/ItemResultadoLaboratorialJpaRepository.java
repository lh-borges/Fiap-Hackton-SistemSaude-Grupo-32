package br.com.fiap.sus.resultados.infrastructure.persistence.repository;

import br.com.fiap.sus.resultados.infrastructure.persistence.entity.ItemResultadoLaboratorialEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemResultadoLaboratorialJpaRepository
        extends JpaRepository<ItemResultadoLaboratorialEntity, UUID> {

    List<ItemResultadoLaboratorialEntity> findByResultadoExameId(UUID resultadoExameId);

    List<ItemResultadoLaboratorialEntity> findByResultadoExameIdIn(Collection<UUID> resultadoExameIds);
}