package br.com.fiap.sus.consultas.infrastructure.persistence.repository;

import br.com.fiap.sus.consultas.infrastructure.persistence.entity.ConsultaEntity;
import java.util.UUID;
import java.util.Set;
import br.com.fiap.sus.consultas.domain.enums.SituacaoConsulta;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ConsultaJpaRepository
        extends JpaRepository<ConsultaEntity, UUID>, JpaSpecificationExecutor<ConsultaEntity> {
    @Query("select distinct c.pacienteId from ConsultaEntity c where c.medicoId = :medicoId and c.situacao <> :excluida")
    Set<UUID> pacientesDoMedico(@Param("medicoId") UUID medicoId, @Param("excluida") SituacaoConsulta excluida);
}
