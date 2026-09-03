package br.com.fiap.sus.cadastros.application.usecase;

import br.com.fiap.sus.cadastros.application.dto.TipoExameOutput;
import br.com.fiap.sus.cadastros.domain.enums.CategoriaExame;
import br.com.fiap.sus.cadastros.domain.model.TipoExame;
import br.com.fiap.sus.cadastros.domain.repository.TipoExameRepository;
import br.com.fiap.sus.shared.domain.exception.ConflitoException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF-05: catalogo de tipos de exame, com categoria e preparo. */
@Service
public class CadastrarTipoExameUseCase {

    private final TipoExameRepository tiposExame;

    public CadastrarTipoExameUseCase(TipoExameRepository tiposExame) {
        this.tiposExame = tiposExame;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public TipoExameOutput executar(String nome, String categoria, String preparo) {
        TipoExame tipoExame = TipoExame.criar(nome, CategoriaExame.de(categoria), preparo);
        if (tiposExame.existePorNome(tipoExame.getNome())) {
            throw new ConflitoException("Ja existe um tipo de exame com este nome.");
        }
        return TipoExameOutput.de(tiposExame.salvar(tipoExame));
    }
}
