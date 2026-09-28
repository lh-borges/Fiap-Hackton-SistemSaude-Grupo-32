package br.com.fiap.sus.historico.infrastructure.fonte;

import br.com.fiap.sus.exames.api.ExameQuery;
import br.com.fiap.sus.exames.api.ExameResumo;
import br.com.fiap.sus.exames.api.SolicitacaoExameResumo;
import br.com.fiap.sus.historico.domain.enums.TipoRegistro;
import br.com.fiap.sus.historico.domain.model.Referencia;
import br.com.fiap.sus.historico.domain.model.RegistroHistorico;
import br.com.fiap.sus.historico.domain.port.FonteHistorico;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Solicitacoes e exames do paciente. A solicitacao usa a data de criacao; o exame usa a data de
 * realizacao ou, se ainda nao realizado, a data agendada (RN-05).
 */
@Component
public class ExamesFonte implements FonteHistorico {

    private final ExameQuery exames;

    public ExamesFonte(ExameQuery exames) {
        this.exames = exames;
    }

    @Override
    public List<RegistroHistorico> registrosDoPaciente(UUID pacienteId) {
        List<RegistroHistorico> registros = new ArrayList<>();
        exames.solicitacoesDoPaciente(pacienteId).forEach(s -> registros.add(registro(s)));
        exames.examesDoPaciente(pacienteId).forEach(e -> registros.add(registro(e)));
        return registros;
    }

    static RegistroHistorico registro(SolicitacaoExameResumo s) {
        return new RegistroHistorico(TipoRegistro.SOLICITACAO_EXAME, s.id(), s.criadoEm(),
                Titulos.comSituacao("Solicitacao de exame", s.situacao()), s.situacao(), s.medicoId(),
                Referencia.opcional(TipoRegistro.CONSULTA, s.consultaId()));
    }

    static RegistroHistorico registro(ExameResumo e) {
        var data = e.dataRealizacao() != null ? e.dataRealizacao() : e.dataAgendada();
        return new RegistroHistorico(TipoRegistro.EXAME, e.id(), data,
                Titulos.comSituacao("Exame", e.situacao()), e.situacao(), e.medicoSolicitanteId(),
                Referencia.opcional(TipoRegistro.SOLICITACAO_EXAME, e.solicitacaoExameId()));
    }
}
