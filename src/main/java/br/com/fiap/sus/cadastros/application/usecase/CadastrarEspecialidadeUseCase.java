package br.com.fiap.sus.cadastros.application.usecase;

import br.com.fiap.sus.cadastros.application.dto.EspecialidadeOutput;
import br.com.fiap.sus.cadastros.domain.model.Especialidade;
import br.com.fiap.sus.cadastros.domain.repository.EspecialidadeRepository;
import br.com.fiap.sus.shared.domain.exception.ConflitoException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF-03. RN-08: nome unico. */
@Service
public class CadastrarEspecialidadeUseCase {

    private final EspecialidadeRepository especialidades;

    public CadastrarEspecialidadeUseCase(EspecialidadeRepository especialidades) {
        this.especialidades = especialidades;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public EspecialidadeOutput executar(String nome, String descricao) {
        Especialidade especialidade = Especialidade.criar(nome, descricao);
        if (especialidades.existePorNome(especialidade.getNome())) {
            throw new ConflitoException("Ja existe uma especialidade com este nome.");
        }
        return EspecialidadeOutput.de(especialidades.salvar(especialidade));
    }
}
