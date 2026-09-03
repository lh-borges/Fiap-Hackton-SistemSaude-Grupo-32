package br.com.fiap.sus.cadastros.application.usecase;

import br.com.fiap.sus.cadastros.application.dto.TipoExameOutput;
import br.com.fiap.sus.cadastros.domain.enums.CategoriaExame;
import br.com.fiap.sus.cadastros.domain.repository.TipoExameRepository;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarTiposExameUseCase {

    private static final int TAMANHO_MAXIMO = 100;

    private final TipoExameRepository tiposExame;

    public ListarTiposExameUseCase(TipoExameRepository tiposExame) {
        this.tiposExame = tiposExame;
    }

    @Transactional(readOnly = true)
    public PaginaResultado<TipoExameOutput> executar(String categoria, Boolean ativo, int pagina, int tamanho) {
        CategoriaExame filtro = categoria == null || categoria.isBlank() ? null : CategoriaExame.de(categoria);
        int tamanhoValido = Math.min(Math.max(tamanho, 1), TAMANHO_MAXIMO);
        return tiposExame.listar(filtro, ativo, Math.max(pagina, 0), tamanhoValido).mapear(TipoExameOutput::de);
    }
}
