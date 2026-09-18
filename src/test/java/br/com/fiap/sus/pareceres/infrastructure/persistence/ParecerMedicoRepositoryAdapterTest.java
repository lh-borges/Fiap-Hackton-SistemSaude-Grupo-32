package br.com.fiap.sus.pareceres.infrastructure.persistence;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.cadastros.api.MedicoResumo;
import br.com.fiap.sus.consultas.domain.enums.SituacaoConsulta;
import br.com.fiap.sus.consultas.infrastructure.persistence.entity.ConsultaEntity;
import br.com.fiap.sus.consultas.infrastructure.persistence.repository.ConsultaQueryAdapter;
import br.com.fiap.sus.pareceres.application.dto.RegistrarParecerDTO;
import br.com.fiap.sus.pareceres.application.service.ParecerQueryService;
import br.com.fiap.sus.pareceres.application.usecase.RegistrarParecerUseCase;
import br.com.fiap.sus.pareceres.domain.model.ParecerMedico;
import br.com.fiap.sus.pareceres.domain.repository.ParecerMedicoFiltro;
import br.com.fiap.sus.pareceres.infrastructure.persistence.mapper.ParecerPersistenceMapper;
import br.com.fiap.sus.pareceres.infrastructure.persistence.repository.ParecerMedicoRepositoryAdapter;
import br.com.fiap.sus.resultados.application.service.ResultadoQueryService;
import br.com.fiap.sus.resultados.domain.enums.TipoResultado;
import br.com.fiap.sus.resultados.infrastructure.persistence.entity.ResultadoExameEntity;
import br.com.fiap.sus.resultados.infrastructure.persistence.mapper.ResultadoPersistenceMapper;
import br.com.fiap.sus.resultados.infrastructure.persistence.repository.ResultadoExameRepositoryAdapter;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"}, showSql = false)
@Import({ParecerMedicoRepositoryAdapter.class, ParecerPersistenceMapper.class, ParecerQueryService.class,
        ResultadoQueryService.class, ResultadoExameRepositoryAdapter.class, ResultadoPersistenceMapper.class,
        ConsultaQueryAdapter.class})
@Sql(statements = "DROP TABLE IF EXISTS par_parecer_medico")
@Sql("classpath:db/migration/V0050__par_parecer_medico.sql")
@Sql("classpath:db/migration/V0051__par_auditoria_e_indice.sql")
class ParecerMedicoRepositoryAdapterTest {
    @Autowired ParecerMedicoRepositoryAdapter repository;
    @Autowired ParecerQueryService pareceres;
    @Autowired ResultadoQueryService resultados;
    @Autowired ConsultaQueryAdapter consultas;
    @Autowired EntityManager entityManager;
    final UUID resultado = UUID.randomUUID();
    final UUID paciente = UUID.randomUUID();
    final UUID medico = UUID.randomUUID();
    final Instant data = Instant.parse("2026-09-17T12:00:00Z");

    ParecerMedico salvar(UUID titular, UUID autor, Instant instante) {
        var parecer = ParecerMedico.reconstituir(UUID.randomUUID(), resultado, titular, autor, "Interpretacao registrada.", instante);
        repository.salvar(parecer, UUID.randomUUID());
        entityManager.flush();
        return parecer;
    }

    @Test
    void migrationAdmiteSegundaOpiniaoERetificacaoEMantemRelacionamentos() {
        var primeiro = salvar(paciente, medico, data);
        salvar(paciente, UUID.randomUUID(), data.plusSeconds(1));
        salvar(paciente, medico, data.plusSeconds(2));
        entityManager.clear();
        assertThat(repository.buscarPorId(primeiro.getId())).hasValueSatisfying(
                salvo -> assertThat(salvo).usingRecursiveComparison().isEqualTo(primeiro));
        assertThat(pareceres.existeParecerParaResultado(resultado)).isTrue();
        assertThat(pareceres.existeParecerParaResultado(UUID.randomUUID())).isFalse();
        var page = repository.listar(new ParecerMedicoFiltro(paciente, null, resultado, null, null), 0, 2);
        assertThat(page.totalElementos()).isEqualTo(3);
        assertThat(page.totalPaginas()).isEqualTo(2);
        assertThat(page.conteudo()).hasSize(2);
        assertThat(page.conteudo().getFirst().getDataParecer()).isEqualTo(data.plusSeconds(2));
    }

    @Test
    void filtraAutorPeriodoETitularAntesDaPaginacao() {
        var permitido = salvar(paciente, medico, data);
        salvar(paciente, UUID.randomUUID(), data);
        salvar(UUID.randomUUID(), medico, data);
        salvar(paciente, medico, data.minusSeconds(1));
        var filtro = new ParecerMedicoFiltro(null, medico, resultado, data, data, Set.of(paciente));
        var page = repository.listar(filtro, 0, 10);
        assertThat(page.totalElementos()).isEqualTo(1);
        assertThat(page.conteudo()).extracting(ParecerMedico::getId).containsExactly(permitido.getId());
        assertThat(repository.listar(new ParecerMedicoFiltro(null, null, null, null, null, Set.of()), 0, 10)
                .totalElementos()).isZero();
    }

    @Test
    void naoSobrescreveParecerExistente() {
        var original = salvar(paciente, medico, data);
        var alterado = ParecerMedico.reconstituir(original.getId(), resultado, paciente, medico,
                "Tentativa de sobrescrita.", data);
        assertThatThrownBy(() -> { repository.salvar(alterado, UUID.randomUUID()); entityManager.flush(); })
                .isInstanceOf(RuntimeException.class);
        assertThat(original.getDescricao()).isEqualTo("Interpretacao registrada.");
    }

    @Test
    void rejeitaPaginacaoForaDosLimites() {
        var filtro = new ParecerMedicoFiltro(null, null, null, null, null);
        assertThatThrownBy(() -> repository.listar(filtro, -1, 20)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> repository.listar(filtro, 0, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> repository.listar(filtro, 0, 101)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void registrarParecerPreservaResultadoByteAByte() throws Exception {
        var resultadoEntity = new ResultadoExameEntity(resultado, UUID.randomUUID(), paciente, TipoResultado.IMAGEM,
                data, "Observacao tecnica", "https://example.org/exame", "Descricao tecnica", "Laudo tecnico");
        entityManager.persist(resultadoEntity);
        entityManager.flush();
        entityManager.clear();
        var json = new ObjectMapper().findAndRegisterModules();
        byte[] antes = json.writeValueAsBytes(entityManager.find(ResultadoExameEntity.class, resultado));
        var cadastros = mock(CadastroQuery.class);
        var usuarios = mock(UsuarioAutenticadoProvider.class);
        UUID usuario = UUID.randomUUID();
        when(usuarios.obrigatorio()).thenReturn(new UsuarioAutenticado(usuario, "Dra. Maria", Set.of("MEDICO")));
        when(cadastros.medicoIdDoUsuario(usuario)).thenReturn(Optional.of(medico));
        when(cadastros.resumoDoMedico(medico)).thenReturn(Optional.of(
                new MedicoResumo(medico, usuario, "1234", "SP", UUID.randomUUID(), true)));
        var registrar = new RegistrarParecerUseCase(repository, resultados, cadastros, usuarios, mock(ApplicationEventPublisher.class));
        var output = registrar.executar(new RegistrarParecerDTO(resultado, "Interpretacao do resultado."));
        entityManager.flush();
        entityManager.clear();
        byte[] depois = json.writeValueAsBytes(entityManager.find(ResultadoExameEntity.class, resultado));
        assertThat(depois).isEqualTo(antes);
        assertThat(output.pacienteId()).isEqualTo(paciente);
        assertThat(resultados.pacienteIdDoResultado(UUID.randomUUID())).isEmpty();
    }

    @Test
    void consultasCanceladasNaoConcedemVinculoAssistencial() {
        UUID cancelado = UUID.randomUUID();
        entityManager.persist(new ConsultaEntity(UUID.randomUUID(), paciente, medico, UUID.randomUUID(), data,
                SituacaoConsulta.REALIZADA, "Consulta", null, null, false, data, data));
        entityManager.persist(new ConsultaEntity(UUID.randomUUID(), cancelado, medico, UUID.randomUUID(), data,
                SituacaoConsulta.CANCELADA, "Consulta", null, "Cancelada", false, data, data));
        entityManager.persist(new ConsultaEntity(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), data,
                SituacaoConsulta.REALIZADA, "Consulta", null, null, false, data, data));
        entityManager.flush();
        assertThat(consultas.pacientesDoMedico(medico)).containsExactly(paciente);
    }
}
