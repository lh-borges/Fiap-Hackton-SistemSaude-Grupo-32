package br.com.fiap.sus.cadastros.application.usecase;

import br.com.fiap.sus.cadastros.application.dto.EspecialidadeOutput;
import br.com.fiap.sus.cadastros.domain.repository.EspecialidadeRepository;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarEspecialidadesUseCase {

    private static final int TAMANHO_MAXIMO = 100;

    private final EspecialidadeRepository especialidades;

    public ListarEspecialidadesUseCase(EspecialidadeRepository especialidades) {
        this.especialidades = especialidades;
    }

    @Transactional(readOnly = true)
    public PaginaResultado<EspecialidadeOutput> executar(Boolean ativo, int pagina, int tamanho) {
        int tamanhoValido = Math.min(Math.max(tamanho, 1), TAMANHO_MAXIMO);
        return especialidades.listar(ativo, Math.max(pagina, 0), tamanhoValido)
                .mapear(EspecialidadeOutput::de);
    }
}
