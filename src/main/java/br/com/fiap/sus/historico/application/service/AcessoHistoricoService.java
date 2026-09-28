package br.com.fiap.sus.historico.application.service;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.consultas.api.ConsultaQuery;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Resolve o paciente cujo historico o usuario autenticado pode ler (spec 010, secao 4).
 * PACIENTE: o proprio, vindo do token (RN-02). MEDICO: paciente com quem tem consulta nao cancelada (RN-03).
 * ADMINISTRADOR: qualquer paciente existente. Acesso negado e tratado como recurso inexistente.
 */
@Service
public class AcessoHistoricoService {

    private final CadastroQuery cadastros;
    private final ConsultaQuery consultas;

    public AcessoHistoricoService(CadastroQuery cadastros, ConsultaQuery consultas) {
        this.cadastros = cadastros;
        this.consultas = consultas;
    }

    public UUID pacienteAutorizado(UsuarioAutenticado usuario, UUID pacienteIdSolicitado) {
        if (usuario.ehPaciente()) {
            return cadastros.pacienteIdDoUsuario(usuario.id())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente nao encontrado para este usuario."));
        }
        if (pacienteIdSolicitado == null) {
            throw new IllegalArgumentException("Informe o paciente cujo historico deseja consultar.");
        }
        if (usuario.ehAdministrador()) {
            if (cadastros.resumoDoPaciente(pacienteIdSolicitado).isEmpty()) {
                throw new RecursoNaoEncontradoException("Paciente nao encontrado.");
            }
            return pacienteIdSolicitado;
        }
        if (usuario.ehMedico()) {
            UUID medicoId = cadastros.medicoIdDoUsuario(usuario.id())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Medico nao encontrado para este usuario."));
            if (!consultas.pacientesDoMedico(medicoId).contains(pacienteIdSolicitado)) {
                throw new RecursoNaoEncontradoException("Paciente nao encontrado.");
            }
            return pacienteIdSolicitado;
        }
        throw new RecursoNaoEncontradoException("Paciente nao encontrado.");
    }
}
