package br.com.fiap.sus.historico.infrastructure.fonte;

import br.com.fiap.sus.consultas.api.ConsultaQuery;
import br.com.fiap.sus.consultas.api.ConsultaResumo;
import br.com.fiap.sus.historico.domain.enums.TipoRegistro;
import br.com.fiap.sus.historico.domain.model.RegistroHistorico;
import br.com.fiap.sus.historico.domain.port.FonteHistorico;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Consultas do paciente; a data do item e o horario da consulta (RN-05). */
@Component
public class ConsultasFonte implements FonteHistorico {

    private final ConsultaQuery consultas;

    public ConsultasFonte(ConsultaQuery consultas) {
        this.consultas = consultas;
    }

    @Override
    public List<RegistroHistorico> registrosDoPaciente(UUID pacienteId) {
        return consultas.consultasDoPaciente(pacienteId).stream().map(ConsultasFonte::registro).toList();
    }

    static RegistroHistorico registro(ConsultaResumo c) {
        String titulo = Titulos.comSituacao("Consulta", c.situacao()) + (c.remarcada() ? " (remarcada)" : "");
        return new RegistroHistorico(TipoRegistro.CONSULTA, c.id(), c.dataHora(), titulo, c.situacao(),
                c.medicoId(), null);
    }
}
