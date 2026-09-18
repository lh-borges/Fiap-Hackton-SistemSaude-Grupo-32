package br.com.fiap.sus.pareceres.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.pareceres.application.dto.ParecerMedicoOutput;
import br.com.fiap.sus.pareceres.application.dto.RegistrarParecerDTO;
import br.com.fiap.sus.pareceres.application.event.ParecerCriadoEvent;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import br.com.fiap.sus.pareceres.domain.model.ParecerMedico;
import br.com.fiap.sus.pareceres.domain.repository.ParecerMedicoRepository;
import br.com.fiap.sus.resultados.api.ResultadoQuery;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class RegistrarParecerUseCase {

    private final ParecerMedicoRepository parecerMedicoRepository;
    private final ResultadoQuery resultadoQuery;
    private final CadastroQuery cadastroQuery;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;
    private final ApplicationEventPublisher eventPublisher;

    public RegistrarParecerUseCase(ParecerMedicoRepository parecerMedicoRepository, ResultadoQuery resultadoQuery,
                                   CadastroQuery cadastroQuery, UsuarioAutenticadoProvider usuarioAutenticadoProvider,
                                   ApplicationEventPublisher eventPublisher) {
        this.parecerMedicoRepository = parecerMedicoRepository;
        this.resultadoQuery = resultadoQuery;
        this.cadastroQuery = cadastroQuery;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
        this.eventPublisher = eventPublisher;
    }

    @PreAuthorize("hasRole('MEDICO')")
    @Transactional
    public ParecerMedicoOutput executar(RegistrarParecerDTO dto) {
        UsuarioAutenticado usuario = usuarioAutenticadoProvider.obrigatorio();

        // RN-01/RN-07: medico deve existir e estar ativo.
        UUID medicoId = cadastroQuery.medicoIdDoUsuario(usuario.id())
                .orElseThrow(() -> new RegraDeNegocioException("Usuario nao e um medico cadastrado."));
        var medico = cadastroQuery.resumoDoMedico(medicoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medico nao encontrado."));
        if (!medico.ativo()) {
            throw new RegraDeNegocioException("Medico inativo nao pode emitir parecer.");
        }

        // RN-02/EX-02: resultado precisa existir; o paciente e derivado dele (RN-03).
        if (dto.resultadoExameId() == null) {
            throw new RegraDeNegocioException("O parecer deve estar vinculado a um resultado de exame.");
        }
        UUID pacienteId = resultadoQuery.pacienteIdDoResultado(dto.resultadoExameId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Resultado nao encontrado."));

        ParecerMedico parecer = ParecerMedico.emitir(dto.resultadoExameId(), pacienteId, medicoId, dto.descricao());
        ParecerMedico salvo = parecerMedicoRepository.salvar(parecer, usuario.id());

        // RF-07: publica o fato para o modulo de notificacoes, sem conteudo clinico.
        eventPublisher.publishEvent(new ParecerCriadoEvent(salvo.getId(), salvo.getResultadoExameId(),
                salvo.getPacienteId(), salvo.getMedicoId(), salvo.getDataParecer()));

        return ParecerMedicoOutput.of(salvo, medico.especialidadeId(), medico.crm(), usuario.nome());
    }
}
