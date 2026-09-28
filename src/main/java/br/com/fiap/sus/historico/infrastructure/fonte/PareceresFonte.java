package br.com.fiap.sus.historico.infrastructure.fonte;

import br.com.fiap.sus.historico.domain.enums.TipoRegistro;
import br.com.fiap.sus.historico.domain.model.Referencia;
import br.com.fiap.sus.historico.domain.model.RegistroHistorico;
import br.com.fiap.sus.historico.domain.port.FonteHistorico;
import br.com.fiap.sus.pareceres.api.ParecerQuery;
import br.com.fiap.sus.pareceres.api.ParecerResumo;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Pareceres do paciente; aponta o resultado analisado. Parecer e imutavel, logo a situacao e fixa. */
@Component
public class PareceresFonte implements FonteHistorico {
    static final String SITUACAO = "EMITIDO";

    private final ParecerQuery pareceres;

    public PareceresFonte(ParecerQuery pareceres) {
        this.pareceres = pareceres;
    }

    @Override
    public List<RegistroHistorico> registrosDoPaciente(UUID pacienteId) {
        return pareceres.pareceresDoPaciente(pacienteId).stream().map(PareceresFonte::registro).toList();
    }

    static RegistroHistorico registro(ParecerResumo p) {
        return new RegistroHistorico(TipoRegistro.PARECER, p.id(), p.dataParecer(), "Parecer medico emitido",
                SITUACAO, p.medicoId(), Referencia.opcional(TipoRegistro.RESULTADO_EXAME, p.resultadoExameId()));
    }
}
