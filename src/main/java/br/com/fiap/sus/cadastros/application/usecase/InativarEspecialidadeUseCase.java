package br.com.fiap.sus.cadastros.application.usecase;

import br.com.fiap.sus.cadastros.domain.model.Especialidade;
import br.com.fiap.sus.cadastros.domain.repository.EspecialidadeRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RN-07: item de catalogo em uso nao e excluido, apenas inativado. */
@Service
public class InativarEspecialidadeUseCase {

    private final EspecialidadeRepository especialidades;

    public InativarEspecialidadeUseCase(EspecialidadeRepository especialidades) {
        this.especialidades = especialidades;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void executar(UUID id) {
        Especialidade especialidade = especialidades.porId(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Especialidade"));
        especialidade.inativar();
        especialidades.salvar(especialidade);
    }
}
