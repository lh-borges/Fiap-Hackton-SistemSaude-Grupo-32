package br.com.fiap.sus.pareceres.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.pareceres.application.dto.RegistrarParecerDTO;
import br.com.fiap.sus.pareceres.application.service.ParecerLeituraService;
import br.com.fiap.sus.pareceres.domain.repository.ParecerMedicoFiltro;
import br.com.fiap.sus.pareceres.domain.repository.ParecerMedicoRepository;
import br.com.fiap.sus.resultados.api.ResultadoQuery;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ParecerAutorizacaoTest.Config.class)
class ParecerAutorizacaoTest {
    @Autowired RegistrarParecerUseCase registrar;
    @Autowired ConsultarParecerUseCase consultar;
    @Autowired ListarPareceresUseCase listar;
    @Autowired ParecerMedicoRepository repository;

    @Configuration
    @EnableMethodSecurity
    static class Config {
        @Bean ParecerMedicoRepository repository() { return mock(ParecerMedicoRepository.class); }
        @Bean RegistrarParecerUseCase registrar(ParecerMedicoRepository repository) {
            return new RegistrarParecerUseCase(repository, mock(ResultadoQuery.class), mock(CadastroQuery.class),
                    mock(UsuarioAutenticadoProvider.class), mock(ApplicationEventPublisher.class));
        }
        @Bean ConsultarParecerUseCase consultar(ParecerMedicoRepository repository) {
            return new ConsultarParecerUseCase(repository, mock(ParecerLeituraService.class), mock(UsuarioAutenticadoProvider.class));
        }
        @Bean ListarPareceresUseCase listar(ParecerMedicoRepository repository) {
            return new ListarPareceresUseCase(repository, mock(ParecerLeituraService.class), mock(UsuarioAutenticadoProvider.class));
        }
    }

    void emissaoNegada() {
        assertThatThrownBy(() -> registrar.executar(new RegistrarParecerDTO(UUID.randomUUID(), "Descricao valida")))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(repository);
    }

    @Test @WithMockUser(roles = "PACIENTE")
    void pacienteNaoEmite() { emissaoNegada(); }

    @Test @WithMockUser(roles = "ATENDENTE")
    void atendenteNaoEmite() { emissaoNegada(); }

    @Test @WithMockUser(roles = "ADMINISTRADOR")
    void administradorSemPerfilMedicoNaoEmite() { emissaoNegada(); }

    @Test @WithMockUser(roles = "ATENDENTE")
    void atendenteNaoConsultaNemLista() {
        assertThatThrownBy(() -> consultar.executar(UUID.randomUUID())).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> listar.executar(new ParecerMedicoFiltro(null, null, null, null, null), 0, 20))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(repository);
    }
}
