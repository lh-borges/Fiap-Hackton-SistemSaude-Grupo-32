package br.com.fiap.sus.historico.application.usecase;

import br.com.fiap.sus.historico.application.service.AcessoHistoricoService;
import br.com.fiap.sus.historico.domain.model.HistoricoFiltro;
import br.com.fiap.sus.historico.domain.model.LinhaDoTempo;
import br.com.fiap.sus.historico.domain.model.RegistroHistorico;
import br.com.fiap.sus.historico.domain.port.FonteHistorico;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** RF-01 a RF-06: monta a linha do tempo do paciente autorizado a partir de todas as fontes. */
@Component
public class ConsultarHistoricoUseCase {

    private final AcessoHistoricoService acesso;
    private final List<FonteHistorico> fontes;
    private final UsuarioAutenticadoProvider usuarios;

    public ConsultarHistoricoUseCase(AcessoHistoricoService acesso, List<FonteHistorico> fontes,
                                     UsuarioAutenticadoProvider usuarios) {
        this.acesso = acesso;
        this.fontes = List.copyOf(fontes);
        this.usuarios = usuarios;
    }

    @PreAuthorize("hasAnyRole('PACIENTE', 'MEDICO', 'ADMINISTRADOR')")
    @Transactional(readOnly = true)
    public PaginaResultado<RegistroHistorico> executar(UUID pacienteIdSolicitado, HistoricoFiltro filtro,
                                                       int pagina, int tamanho) {
        UUID pacienteId = acesso.pacienteAutorizado(usuarios.obrigatorio(), pacienteIdSolicitado);
        List<RegistroHistorico> registros = fontes.stream()
                .flatMap(fonte -> fonte.registrosDoPaciente(pacienteId).stream())
                .toList();
        return LinhaDoTempo.de(registros)
                .filtrar(filtro == null ? HistoricoFiltro.todos() : filtro)
                .pagina(pagina, tamanho);
    }
}
