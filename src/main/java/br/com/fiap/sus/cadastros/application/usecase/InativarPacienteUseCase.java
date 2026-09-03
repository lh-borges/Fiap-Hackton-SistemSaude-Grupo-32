package br.com.fiap.sus.cadastros.application.usecase;

import br.com.fiap.sus.cadastros.domain.model.Paciente;
import br.com.fiap.sus.cadastros.domain.repository.PacienteRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU-05: inativacao preserva o historico (RN-06 e Artigo V.6). */
@Service
public class InativarPacienteUseCase {

    private final PacienteRepository pacientes;

    public InativarPacienteUseCase(PacienteRepository pacientes) {
        this.pacientes = pacientes;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENDENTE')")
    public void executar(UUID id) {
        Paciente paciente = pacientes.porId(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Paciente"));
        paciente.inativar();
        pacientes.salvar(paciente);
    }
}
