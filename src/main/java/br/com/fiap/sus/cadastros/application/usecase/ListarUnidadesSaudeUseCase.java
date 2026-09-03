package br.com.fiap.sus.cadastros.application.usecase;

import br.com.fiap.sus.cadastros.application.dto.UnidadeSaudeOutput;
import br.com.fiap.sus.cadastros.domain.repository.UnidadeSaudeRepository;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarUnidadesSaudeUseCase {

    private static final int TAMANHO_MAXIMO = 100;

    private final UnidadeSaudeRepository unidades;

    public ListarUnidadesSaudeUseCase(UnidadeSaudeRepository unidades) {
        this.unidades = unidades;
    }

    @Transactional(readOnly = true)
    public PaginaResultado<UnidadeSaudeOutput> executar(Boolean ativo, int pagina, int tamanho) {
        int tamanhoValido = Math.min(Math.max(tamanho, 1), TAMANHO_MAXIMO);
        return unidades.listar(ativo, Math.max(pagina, 0), tamanhoValido).mapear(UnidadeSaudeOutput::de);
    }
}
