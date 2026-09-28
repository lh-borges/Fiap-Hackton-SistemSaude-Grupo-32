package br.com.fiap.sus.historico.infrastructure.fonte;

import br.com.fiap.sus.historico.domain.enums.TipoRegistro;
import br.com.fiap.sus.historico.domain.model.Referencia;
import br.com.fiap.sus.historico.domain.model.RegistroHistorico;
import br.com.fiap.sus.historico.domain.port.FonteHistorico;
import br.com.fiap.sus.resultados.api.ResultadoQuery;
import br.com.fiap.sus.resultados.api.ResultadoResumo;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Resultados do paciente. Laudo, descricao e arquivo sao descartados (RN-06). */
@Component
public class ResultadosFonte implements FonteHistorico {
    static final String SITUACAO = "DISPONIVEL";

    private final ResultadoQuery resultados;

    public ResultadosFonte(ResultadoQuery resultados) {
        this.resultados = resultados;
    }

    @Override
    public List<RegistroHistorico> registrosDoPaciente(UUID pacienteId) {
        return resultados.resultadosDoPaciente(pacienteId).stream().map(ResultadosFonte::registro).toList();
    }

    static RegistroHistorico registro(ResultadoResumo r) {
        return new RegistroHistorico(TipoRegistro.RESULTADO_EXAME, r.id(), r.dataResultado(),
                Titulos.comQualificador("Resultado de exame", r.tipoResultado()), SITUACAO, null,
                Referencia.opcional(TipoRegistro.EXAME, r.exameId()));
    }
}
