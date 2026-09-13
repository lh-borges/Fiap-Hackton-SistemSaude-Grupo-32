package br.com.fiap.sus.exames.infrastructure.persistence.mapper;

import br.com.fiap.sus.exames.domain.model.Exame;
import br.com.fiap.sus.exames.domain.model.SolicitacaoExame;
import br.com.fiap.sus.exames.infrastructure.persistence.entity.ExameEntity;
import br.com.fiap.sus.exames.infrastructure.persistence.entity.SolicitacaoExameEntity;
import org.springframework.stereotype.Component;

@Component
public class ExamePersistenceMapper {

    public SolicitacaoExame paraDominio(SolicitacaoExameEntity e) {
        return SolicitacaoExame.reconstituir(e.getId(), e.getPacienteId(), e.getMedicoId(),
                e.getTipoExameId(), e.getConsultaId(), e.getJustificativa(), e.getSituacao(),
                e.getMotivoCancelamento(), e.getCriadoEm(), e.getAtualizadoEm());
    }

    public SolicitacaoExameEntity paraEntidade(SolicitacaoExame s) {
        return new SolicitacaoExameEntity(s.getId(), s.getPacienteId(), s.getMedicoId(), s.getTipoExameId(),
                s.getConsultaId(), s.getJustificativa(), s.getSituacao(), s.getMotivoCancelamento(),
                s.getCriadoEm(), s.getAtualizadoEm());
    }

    public Exame paraDominio(ExameEntity e) {
        return Exame.reconstituir(e.getId(), e.getSolicitacaoExameId(), e.getUnidadeSaudeId(),
                e.getDataAgendada(), e.getDataRealizacao(), e.getSituacao(), e.getCriadoEm(),
                e.getAtualizadoEm());
    }

    public ExameEntity paraEntidade(Exame ex) {
        return new ExameEntity(ex.getId(), ex.getSolicitacaoExameId(), ex.getUnidadeSaudeId(),
                ex.getDataAgendada(), ex.getDataRealizacao(), ex.getSituacao(), ex.getCriadoEm(),
                ex.getAtualizadoEm());
    }
}