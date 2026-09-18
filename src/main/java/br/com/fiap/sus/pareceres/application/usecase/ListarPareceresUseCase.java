package br.com.fiap.sus.pareceres.application.usecase;

import br.com.fiap.sus.pareceres.application.dto.ParecerMedicoOutput;
import br.com.fiap.sus.pareceres.application.service.ParecerLeituraService;
import br.com.fiap.sus.pareceres.domain.repository.ParecerMedicoFiltro;
import br.com.fiap.sus.pareceres.domain.repository.ParecerMedicoRepository;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class ListarPareceresUseCase {
    private final ParecerMedicoRepository repository;
    private final ParecerLeituraService leitura;
    private final UsuarioAutenticadoProvider usuarios;

    public ListarPareceresUseCase(ParecerMedicoRepository repository, ParecerLeituraService leitura,
                                  UsuarioAutenticadoProvider usuarios) {
        this.repository = repository;
        this.leitura = leitura;
        this.usuarios = usuarios;
    }

    @PreAuthorize("hasAnyRole('MEDICO', 'PACIENTE', 'ADMINISTRADOR')")
    public PaginaResultado<ParecerMedicoOutput> executar(ParecerMedicoFiltro filtro, int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 100) {
            throw new IllegalArgumentException("Pagina deve ser positiva ou zero e tamanho entre 1 e 100.");
        }
        if (filtro.periodoInicio() != null && filtro.periodoFim() != null
                && filtro.periodoInicio().isAfter(filtro.periodoFim())) {
            throw new IllegalArgumentException("O inicio do periodo deve ser anterior ou igual ao fim.");
        }
        var usuario = usuarios.obrigatorio();
        return repository.listar(leitura.restringir(filtro, usuario), pagina, tamanho)
                .mapear(parecer -> leitura.output(parecer, usuario));
    }
}
