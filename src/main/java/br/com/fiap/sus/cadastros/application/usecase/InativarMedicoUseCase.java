package br.com.fiap.sus.cadastros.application.usecase;

import br.com.fiap.sus.cadastros.domain.model.Medico;
import br.com.fiap.sus.cadastros.domain.repository.MedicoRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU-05: o medico inativado nao entra em novos agendamentos, mas seus registros ficam. */
@Service
public class InativarMedicoUseCase {

    private final MedicoRepository medicos;

    public InativarMedicoUseCase(MedicoRepository medicos) {
        this.medicos = medicos;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void executar(UUID id) {
        Medico medico = medicos.porId(id).orElseThrow(() -> RecursoNaoEncontradoException.de("Medico"));
        medico.inativar();
        medicos.salvar(medico);
    }
}
