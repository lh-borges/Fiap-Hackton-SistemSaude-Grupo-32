package br.com.fiap.sus.historico.infrastructure.fonte;

import br.com.fiap.sus.historico.domain.enums.TipoRegistro;
import br.com.fiap.sus.historico.domain.model.Referencia;
import br.com.fiap.sus.historico.domain.model.RegistroHistorico;
import br.com.fiap.sus.historico.domain.port.FonteHistorico;
import br.com.fiap.sus.receitas.api.ReceitaQuery;
import br.com.fiap.sus.receitas.api.ReceitaResumo;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Receitas do paciente; uma renovacao aponta a receita de origem. Itens nao sao expostos (RN-06). */
@Component
public class ReceitasFonte implements FonteHistorico {

    private final ReceitaQuery receitas;

    public ReceitasFonte(ReceitaQuery receitas) {
        this.receitas = receitas;
    }

    @Override
    public List<RegistroHistorico> registrosDoPaciente(UUID pacienteId) {
        return receitas.receitasDoPaciente(pacienteId).stream().map(ReceitasFonte::registro).toList();
    }

    static RegistroHistorico registro(ReceitaResumo r) {
        String titulo = r.receitaOrigemId() != null
                ? Titulos.comSituacao("Receita renovada,", r.situacao())
                : Titulos.comSituacao("Receita", r.situacao());
        return new RegistroHistorico(TipoRegistro.RECEITA, r.id(), r.dataEmissao(), titulo, r.situacao(),
                r.medicoId(), Referencia.opcional(TipoRegistro.RECEITA, r.receitaOrigemId()));
    }
}
