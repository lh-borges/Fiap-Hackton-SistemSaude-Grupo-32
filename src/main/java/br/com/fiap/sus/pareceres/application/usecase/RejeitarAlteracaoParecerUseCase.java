package br.com.fiap.sus.pareceres.application.usecase;

import br.com.fiap.sus.pareceres.application.service.ParecerLeituraService;
import br.com.fiap.sus.pareceres.domain.repository.ParecerMedicoRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class RejeitarAlteracaoParecerUseCase {
    private final ParecerMedicoRepository repository;
    private final ParecerLeituraService leitura;
    private final UsuarioAutenticadoProvider usuarios;

    public RejeitarAlteracaoParecerUseCase(
            ParecerMedicoRepository repository,
            ParecerLeituraService leitura,
            UsuarioAutenticadoProvider usuarios
    ) {
        this.repository = repository;
        this.leitura = leitura;
        this.usuarios = usuarios;
    }

    @PreAuthorize("hasAnyRole('MEDICO', 'PACIENTE', 'ADMINISTRADOR')")
    public void executar(UUID parecerId) {
        var parecer = repository.buscarPorId(parecerId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Parecer não encontrado."));
        leitura.verificarAcesso(parecer, usuarios.obrigatorio());
        throw new RegraDeNegocioException(
                "Parecer médico é imutável e não pode ser alterado ou excluido. Registre um novo parecer retificador.");
    }
}
