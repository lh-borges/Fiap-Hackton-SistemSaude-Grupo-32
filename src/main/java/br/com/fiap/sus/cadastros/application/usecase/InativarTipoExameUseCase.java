package br.com.fiap.sus.cadastros.application.usecase;

import br.com.fiap.sus.cadastros.domain.model.TipoExame;
import br.com.fiap.sus.cadastros.domain.repository.TipoExameRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InativarTipoExameUseCase {

    private final TipoExameRepository tiposExame;

    public InativarTipoExameUseCase(TipoExameRepository tiposExame) {
        this.tiposExame = tiposExame;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void executar(UUID id) {
        TipoExame tipoExame = tiposExame.porId(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Tipo de exame"));
        tipoExame.inativar();
        tiposExame.salvar(tipoExame);
    }
}
