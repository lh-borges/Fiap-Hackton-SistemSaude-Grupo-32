package br.com.fiap.sus.cadastros.application.usecase;

import br.com.fiap.sus.cadastros.application.dto.PacienteOutput;
import br.com.fiap.sus.cadastros.domain.repository.PacienteRepository;
import br.com.fiap.sus.iam.api.IamQuery;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RF-06: busca por nome, CPF ou cartao SUS. O paciente nao lista outros pacientes.
 *
 * <p>Nome e CPF pertencem ao modulo iam. Em vez de fazer join entre modulos, o termo e
 * resolvido pela porta {@code IamQuery} e os ids resultantes entram como filtro local.
 */
@Service
public class ListarPacientesUseCase {

    private static final int TAMANHO_MAXIMO = 100;
    private static final int LIMITE_CANDIDATOS = 500;

    private final PacienteRepository pacientes;
    private final IamQuery iam;

    public ListarPacientesUseCase(PacienteRepository pacientes, IamQuery iam) {
        this.pacientes = pacientes;
        this.iam = iam;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENDENTE', 'MEDICO')")
    public PaginaResultado<PacienteOutput> executar(String termo, Boolean ativo, int pagina, int tamanho) {
        String termoLimpo = termo == null || termo.isBlank() ? null : termo.trim();
        List<UUID> usuariosDoTermo = termoLimpo == null
                ? List.of()
                : iam.idsDeUsuariosPorTermo(termoLimpo, LIMITE_CANDIDATOS);

        int tamanhoValido = Math.min(Math.max(tamanho, 1), TAMANHO_MAXIMO);
        return pacientes.buscar(termoLimpo, usuariosDoTermo, ativo, Math.max(pagina, 0), tamanhoValido)
                .mapear(paciente -> {
                    var usuario = iam.resumoDoUsuario(paciente.getUsuarioId()).orElse(null);
                    return PacienteOutput.de(paciente,
                            usuario == null ? null : usuario.nome(),
                            usuario == null ? null : usuario.email());
                });
    }
}
