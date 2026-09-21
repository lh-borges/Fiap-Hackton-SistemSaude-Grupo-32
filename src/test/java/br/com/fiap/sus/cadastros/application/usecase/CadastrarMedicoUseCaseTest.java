package br.com.fiap.sus.cadastros.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.fiap.sus.cadastros.application.dto.CadastrarMedicoInput;
import br.com.fiap.sus.cadastros.application.dto.MedicoOutput;
import br.com.fiap.sus.cadastros.domain.model.Especialidade;
import br.com.fiap.sus.cadastros.domain.model.Medico;
import br.com.fiap.sus.cadastros.domain.repository.EspecialidadeRepository;
import br.com.fiap.sus.cadastros.domain.repository.MedicoRepository;
import br.com.fiap.sus.iam.api.IamQuery;
import br.com.fiap.sus.iam.api.UsuarioResumo;
import br.com.fiap.sus.shared.domain.exception.ConflitoException;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cadastro de medico (HU-02)")
class CadastrarMedicoUseCaseTest {

    private static final UUID USUARIO = UUID.randomUUID();
    private static final UUID ESPECIALIDADE_ID = UUID.randomUUID();

    @Mock
    private MedicoRepository medicos;
    @Mock
    private EspecialidadeRepository especialidades;
    @Mock
    private IamQuery iam;

    private CadastrarMedicoUseCase cadastrar;

    @BeforeEach
    void preparar() {
        cadastrar = new CadastrarMedicoUseCase(medicos, especialidades, iam);
    }

    @Test
    @DisplayName("cria o medico e enriquece a saida com usuario e especialidade")
    void criaMedico() {
        Especialidade especialidade = Especialidade.reconstituir(
                ESPECIALIDADE_ID, "Clinica Geral", "Atendimento primario", true, null, null);
        when(iam.usuarioAtivoPossuiRole(USUARIO, "MEDICO")).thenReturn(true);
        when(medicos.existePorUsuarioId(USUARIO)).thenReturn(false);
        when(especialidades.porId(ESPECIALIDADE_ID)).thenReturn(Optional.of(especialidade));
        when(medicos.existePorCrmEUf("123456", "SP")).thenReturn(false);
        when(medicos.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));
        when(iam.resumoDoUsuario(USUARIO)).thenReturn(Optional.of(
                new UsuarioResumo(USUARIO, "Carlos Lima", "222.333.444-55",
                        "carlos.lima@sus.gov.br", true, Set.of("MEDICO"))));

        MedicoOutput saida = cadastrar.executar(entrada());

        assertThat(saida.nome()).isEqualTo("Carlos Lima");
        assertThat(saida.email()).isEqualTo("carlos.lima@sus.gov.br");
        assertThat(saida.crm()).isEqualTo("123456");
        assertThat(saida.ufCrm()).isEqualTo("SP");
        assertThat(saida.especialidade()).isEqualTo("Clinica Geral");
        assertThat(saida.ativo()).isTrue();
    }

    @Test
    @DisplayName("usuario sem perfil MEDICO e rejeitado")
    void usuarioSemPerfilMedicoEhRejeitado() {
        when(iam.usuarioAtivoPossuiRole(USUARIO, "MEDICO")).thenReturn(false);

        assertThatThrownBy(() -> cadastrar.executar(entrada()))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("MEDICO");
        verify(medicos, never()).salvar(any());
    }

    @Test
    @DisplayName("usuario que ja tem cadastro de medico gera conflito")
    void usuarioComCadastroGeraConflito() {
        when(iam.usuarioAtivoPossuiRole(USUARIO, "MEDICO")).thenReturn(true);
        when(medicos.existePorUsuarioId(USUARIO)).thenReturn(true);

        assertThatThrownBy(() -> cadastrar.executar(entrada()))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("ja possui cadastro");
        verify(medicos, never()).salvar(any());
    }

    @Test
    @DisplayName("CRM duplicado na mesma UF gera conflito")
    void crmDuplicadoNaUfGeraConflito() {
        Especialidade especialidade = Especialidade.criar("Clinica Geral", null);
        when(iam.usuarioAtivoPossuiRole(USUARIO, "MEDICO")).thenReturn(true);
        when(medicos.existePorUsuarioId(USUARIO)).thenReturn(false);
        when(especialidades.porId(ESPECIALIDADE_ID)).thenReturn(Optional.of(especialidade));
        when(medicos.existePorCrmEUf("123456", "SP")).thenReturn(true);

        assertThatThrownBy(() -> cadastrar.executar(entrada()))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("CRM");
        verify(medicos, never()).salvar(any(Medico.class));
    }

    private CadastrarMedicoInput entrada() {
        return new CadastrarMedicoInput(USUARIO, "123456", "SP", ESPECIALIDADE_ID);
    }
}
