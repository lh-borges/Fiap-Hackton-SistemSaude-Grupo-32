package br.com.fiap.sus.consultas.infrastructure.persistence.mapper;

import br.com.fiap.sus.consultas.domain.model.Consulta;
import br.com.fiap.sus.consultas.infrastructure.persistence.entity.ConsultaEntity;
import org.springframework.stereotype.Component;

@Component
public class ConsultaPersistenceMapper {

    public Consulta paraDominio(ConsultaEntity e) {
        return Consulta.reconstituir(e.getId(), e.getPacienteId(), e.getMedicoId(), e.getUnidadeSaudeId(),
                e.getDataHora(), e.getSituacao(), e.getMotivo(), e.getObservacoes(),
                e.getMotivoCancelamento(), e.isRemarcada(), e.getCriadoEm(), e.getAtualizadoEm());
    }

    public ConsultaEntity paraEntidade(Consulta c) {
        return new ConsultaEntity(c.getId(), c.getPacienteId(), c.getMedicoId(), c.getUnidadeSaudeId(),
                c.getDataHora(), c.getSituacao(), c.getMotivo(), c.getObservacoes(),
                c.getMotivoCancelamento(), c.isRemarcada(), c.getCriadoEm(), c.getAtualizadoEm());
    }
}