package br.com.fiap.sus.cadastros.application.usecase;

import br.com.fiap.sus.cadastros.application.dto.UnidadeSaudeOutput;
import br.com.fiap.sus.cadastros.domain.model.UnidadeSaude;
import br.com.fiap.sus.cadastros.domain.repository.UnidadeSaudeRepository;
import br.com.fiap.sus.shared.domain.exception.ConflitoException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF-04. RN-08: CNES unico. */
@Service
public class CadastrarUnidadeSaudeUseCase {

    private final UnidadeSaudeRepository unidades;

    public CadastrarUnidadeSaudeUseCase(UnidadeSaudeRepository unidades) {
        this.unidades = unidades;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public UnidadeSaudeOutput executar(String nome, String cnes, String telefone) {
        UnidadeSaude unidade = UnidadeSaude.criar(nome, cnes, telefone);
        if (unidades.existePorCnes(unidade.getCnes())) {
            throw new ConflitoException("Ja existe uma unidade de saude com este CNES.");
        }
        return UnidadeSaudeOutput.de(unidades.salvar(unidade));
    }
}
