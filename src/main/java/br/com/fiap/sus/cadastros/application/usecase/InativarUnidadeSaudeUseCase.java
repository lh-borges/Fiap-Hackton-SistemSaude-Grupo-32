package br.com.fiap.sus.cadastros.application.usecase;

import br.com.fiap.sus.cadastros.domain.model.UnidadeSaude;
import br.com.fiap.sus.cadastros.domain.repository.UnidadeSaudeRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InativarUnidadeSaudeUseCase {

    private final UnidadeSaudeRepository unidades;

    public InativarUnidadeSaudeUseCase(UnidadeSaudeRepository unidades) {
        this.unidades = unidades;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void executar(UUID id) {
        UnidadeSaude unidade = unidades.porId(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Unidade de saude"));
        unidade.inativar();
        unidades.salvar(unidade);
    }
}
