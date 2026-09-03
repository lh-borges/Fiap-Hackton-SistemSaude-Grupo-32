package br.com.fiap.sus.cadastros.application.usecase;

import br.com.fiap.sus.cadastros.application.dto.MedicoOutput;
import br.com.fiap.sus.cadastros.domain.repository.MedicoRepository;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF-07: listagem de medicos com filtro por especialidade. */
@Service
public class ListarMedicosUseCase {

    private static final int TAMANHO_MAXIMO = 100;

    private final MedicoRepository medicos;
    private final BuscarMedicoUseCase buscarMedico;

    public ListarMedicosUseCase(MedicoRepository medicos, BuscarMedicoUseCase buscarMedico) {
        this.medicos = medicos;
        this.buscarMedico = buscarMedico;
    }

    @Transactional(readOnly = true)
    public PaginaResultado<MedicoOutput> executar(UUID especialidadeId, Boolean ativo, int pagina, int tamanho) {
        int tamanhoValido = Math.min(Math.max(tamanho, 1), TAMANHO_MAXIMO);
        return medicos.buscar(especialidadeId, ativo, Math.max(pagina, 0), tamanhoValido)
                .mapear(buscarMedico::montar);
    }
}
