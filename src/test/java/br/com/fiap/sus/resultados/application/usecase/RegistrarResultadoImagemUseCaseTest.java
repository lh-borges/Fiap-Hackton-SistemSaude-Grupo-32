package br.com.fiap.sus.resultados.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.exames.api.ExameQuery;
import br.com.fiap.sus.resultados.application.dto.RegistrarResultadoImagemDTO;
import br.com.fiap.sus.resultados.application.dto.ResultadoExameOutput;
import br.com.fiap.sus.resultados.domain.enums.TipoResultado;
import br.com.fiap.sus.resultados.domain.model.ResultadoExame;
import br.com.fiap.sus.resultados.domain.repository.ResultadoExameRepository;
import br.com.fiap.sus.shared.domain.exception.ConflitoException;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistrarResultadoImagemUseCaseTest {

    @Mock
    private ResultadoExameRepository resultadoExameRepository;

    @Mock
    private ExameQuery exameQuery;

    @Mock
    private CadastroQuery cadastroQuery;

    private RegistrarResultadoImagemUseCase useCase;

    private final UUID exameId = UUID.randomUUID();
    private final UUID pacienteId = UUID.randomUUID();
    private final UUID tipoExameId = UUID.randomUUID();

    @BeforeEach
    void configurar() {
        useCase = new RegistrarResultadoImagemUseCase(resultadoExameRepository, exameQuery, cadastroQuery);
    }

    private RegistrarResultadoImagemDTO dtoValido() {
        return new RegistrarResultadoImagemDTO(exameId, pacienteId, "s3://bucket/raio-x.png",
                "Raio-X de torax", "Sem alteracoes", "Observacao");
    }

    @Test
    @DisplayName("registra resultado de imagem para exame realizado e tipo compativel")
    void registraComExameRealizadoETipoCompativel() {
        when(exameQuery.exameRealizadoExiste(exameId)).thenReturn(true);
        when(resultadoExameRepository.existePorExameId(exameId)).thenReturn(false);
        when(exameQuery.tipoExameIdDoExame(exameId)).thenReturn(Optional.of(tipoExameId));
        when(cadastroQuery.categoriaDoTipoExame(tipoExameId)).thenReturn(Optional.of("IMAGEM"));
        when(resultadoExameRepository.salvar(any(ResultadoExame.class))).thenAnswer(inv -> inv.getArgument(0));

        ResultadoExameOutput resultado = useCase.executar(dtoValido());

        assertThat(resultado.tipoResultado()).isEqualTo(TipoResultado.IMAGEM);
    }

    @Test
    @DisplayName("nao registra se o exame nao estiver realizado")
    void naoRegistraSeExameNaoRealizado() {
        when(exameQuery.exameRealizadoExiste(exameId)).thenReturn(false);

        assertThatThrownBy(() -> useCase.executar(dtoValido()))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("nao registra se ja existir resultado para o exame")
    void naoRegistraSeJaExisteResultado() {
        when(exameQuery.exameRealizadoExiste(exameId)).thenReturn(true);
        when(resultadoExameRepository.existePorExameId(exameId)).thenReturn(true);

        assertThatThrownBy(() -> useCase.executar(dtoValido()))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    @DisplayName("nao registra se a categoria do tipo de exame nao for IMAGEM")
    void naoRegistraSeCategoriaIncompativel() {
        when(exameQuery.exameRealizadoExiste(exameId)).thenReturn(true);
        when(resultadoExameRepository.existePorExameId(exameId)).thenReturn(false);
        when(exameQuery.tipoExameIdDoExame(exameId)).thenReturn(Optional.of(tipoExameId));
        when(cadastroQuery.categoriaDoTipoExame(tipoExameId)).thenReturn(Optional.of("LABORATORIAL"));

        assertThatThrownBy(() -> useCase.executar(dtoValido()))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("nao registra se o exame nao for encontrado ao resolver o tipo")
    void naoRegistraSeExameNaoEncontradoAoResolverTipo() {
        when(exameQuery.exameRealizadoExiste(exameId)).thenReturn(true);
        when(resultadoExameRepository.existePorExameId(exameId)).thenReturn(false);
        when(exameQuery.tipoExameIdDoExame(exameId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(dtoValido()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}