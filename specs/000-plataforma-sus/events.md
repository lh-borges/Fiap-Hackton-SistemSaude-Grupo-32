# Catálogo de Eventos

Contrato entre os módulos produtores e o módulo `notificacoes`. Este arquivo é a fonte de
verdade: `009-notificacoes` é desenvolvido contra ele.

> Revisado em 2026-09-25 (Danilo) para refletir o que já existe no código: `pareceres` e
> `documentos` publicam eventos com nomes próprios, que foram mantidos. Os demais eventos
> ainda serão criados pelos produtores (seção "Estado por produtor").

## Regras

1. Nome da classe: `<Agregado><FatoNoPassado>Event`, `record` imutável.
2. Pacote: `<modulo>.application.event`, exportado como `@NamedInterface("events")` no
   `package-info.java` desse pacote. Não fica em `domain` porque `@Externalized` é anotação
   do Spring Modulith e o `domain` é livre de framework (Artigo II.2); não fica em `api`
   porque a porta de leitura e o contrato de evento evoluem em ritmos diferentes.
3. Todo evento implementa `br.com.fiap.sus.shared.domain.event.EventoDominio`, com
   `eventoId` (UUID novo por publicação) e `ocorridoEm` (Instant UTC).
4. **Payload não carrega conteúdo clínico** — sem laudo, sem valor de exame, sem
   diagnóstico, sem medicamento. Só identificadores e metadados de navegação.
5. Publicado pelo caso de uso via `ApplicationEventPublisher` **dentro de um método
   `@Transactional`**. O listener do Modulith é transacional: evento publicado fora de
   transação é descartado silenciosamente. A entrega ocorre após o commit.
6. Externalizado para Kafka com `@Externalized("sus.<modulo>.<evento>.v1::#{pacienteId()}")`
   (o `pacienteId` é a chave da partição, preservando a ordem por paciente).
7. Consumidor é idempotente por `eventoId` (tabela `not_evento_processado`).
8. Evolução: campo novo é opcional; mudança incompatível cria a versão `.v2` do tópico.

## Envelope comum

```java
package br.com.fiap.sus.shared.domain.event;

public interface EventoDominio {
    UUID eventoId();
    Instant ocorridoEm();
}
```

## Eventos

| Evento | Produtor (caso de uso) | Tópico | Payload além do envelope | Notificação para |
|---|---|---|---|---|
| `ConsultaAgendadaEvent` | `consultas` · `AgendarConsultaUseCase` | `sus.consultas.agendada.v1` | `consultaId`, `pacienteId`, `medicoId`, `unidadeSaudeId`, `dataHora` | paciente e médico |
| `ConsultaRemarcadaEvent` | `consultas` · `RemarcarConsultaUseCase` | `sus.consultas.remarcada.v1` | `consultaId`, `pacienteId`, `medicoId`, `dataHoraAnterior`, `dataHoraNova` | paciente e médico |
| `ConsultaCanceladaEvent` | `consultas` · `CancelarConsultaUseCase` | `sus.consultas.cancelada.v1` | `consultaId`, `pacienteId`, `medicoId`, `dataHora`, `canceladaPorUsuarioId` | paciente e médico |
| `ExameSolicitadoEvent` | `exames` · `SolicitarExameUseCase` | `sus.exames.solicitado.v1` | `solicitacaoId`, `pacienteId`, `medicoId`, `tipoExameId` | paciente |
| `ExameAgendadoEvent` | `exames` · `AgendarExameUseCase` | `sus.exames.agendado.v1` | `exameId`, `solicitacaoId`, `pacienteId`, `unidadeSaudeId`, `dataAgendada` | paciente |
| `ResultadoExameDisponivelEvent` | `resultados` · `RegistrarResultadoImagemUseCase` e `RegistrarResultadoLaboratorialUseCase` | `sus.resultados.disponivel.v1` | `resultadoId`, `exameId`, `pacienteId`, `medicoSolicitanteId`, `tipoResultado` | paciente e médico solicitante |
| `ParecerCriadoEvent` (já existe) | `pareceres` · `RegistrarParecerUseCase` | `sus.pareceres.criado.v1` | `parecerId`, `resultadoExameId`, `pacienteId`, `medicoId`, `dataParecer` | paciente |
| `ReceitaEmitidaEvent` | `receitas` · `EmitirReceitaUseCase` | `sus.receitas.emitida.v1` | `receitaId`, `pacienteId`, `medicoId`, `validade`, `quantidadeItens` | paciente |
| `ReceitaRenovadaEvent` | `receitas` · `RenovarReceitaUseCase` | `sus.receitas.renovada.v1` | `receitaId`, `receitaOrigemId`, `pacienteId`, `medicoId` | paciente |
| `DocumentoEmitidoEvent` (já existe) | `documentos` · `EmitirDocumentoUseCase` | `sus.documentos.emitido.v1` | `documentoId`, `pacienteId`, `medicoId`, `tipo`, `dataEmissao` | paciente |

`ExameRealizadoEvent`, previsto na versão anterior deste catálogo como "gatilho interno",
**não é implementado no MVP**: não tem consumidor (Artigo X.1).

### `medicoSolicitanteId` do resultado

O módulo `resultados` não guarda o médico solicitante; ele está na solicitação de exame.
A porta `exames::api` ganha o método `Optional<UUID> medicoSolicitanteIdDoExame(UUID exameId)`,
que `resultados` usa para preencher o evento.

## Estado por produtor (2026-09-25)

| Módulo | Situação | O que falta |
|---|---|---|
| `consultas` | não publica | 3 eventos, `@Transactional` nos 3 casos de uso, `package-info` do pacote `events` |
| `exames` | não publica | 2 eventos, `@Transactional`, `package-info`, método novo em `ExameQuery` |
| `resultados` | não publica | 1 evento publicado por 2 casos de uso, `@Transactional`, `package-info` |
| `pareceres` | publica `ParecerCriadoEvent` com `@NamedInterface("events")` | `eventoId`, `ocorridoEm`, `EventoDominio`, `@Externalized` |
| `receitas` | não publica | 2 eventos, `@Transactional`, `package-info` |
| `documentos` | publica `DocumentoEmitidoEvent` sem exportar o pacote | `package-info` com `@NamedInterface("events")`, `eventoId`, `ocorridoEm`, `EventoDominio`, `@Externalized`, `@Transactional` |

## Exemplo

```java
package br.com.fiap.sus.resultados.application.event;

@Externalized("sus.resultados.disponivel.v1::#{pacienteId()}")
public record ResultadoExameDisponivelEvent(
        UUID eventoId,
        Instant ocorridoEm,
        UUID resultadoId,
        UUID exameId,
        UUID pacienteId,
        UUID medicoSolicitanteId,
        String tipoResultado
) implements EventoDominio {

    public static ResultadoExameDisponivelEvent de(ResultadoExame r, UUID medicoSolicitanteId) {
        return new ResultadoExameDisponivelEvent(
                UUID.randomUUID(), Instant.now(),
                r.getId(), r.getExameId(), r.getPacienteId(),
                medicoSolicitanteId, r.getTipoResultado().name());
    }
}
```

## Consumo pelo módulo `notificacoes`

- O listener é `@ApplicationModuleListener` (após commit, assíncrono, transação própria).
  É ele quem gera a notificação, com ou sem Kafka.
- O Kafka recebe a **externalização** do evento (integração e demonstração). O MVP não
  consome do Kafka para gerar notificação: seria um segundo caminho para o mesmo efeito.
- `notificacoes` declara dependência em `<modulo>::events` de cada produtor e em
  `cadastros::api` (para converter `pacienteId`/`medicoId` no `usuarioId` destinatário).

## Degradação sem Kafka

`spring.modulith.events.externalization.enabled=false` (padrão no perfil `local`) desliga a
externalização; o listener continua recebendo o evento após o commit e a notificação é
gerada. No perfil `docker` a externalização é ligada e `spring.kafka.bootstrap-servers`
aponta para o broker do Compose. O registro de publicações (`event_publication`) garante
reentrega de eventos não concluídos na próxima subida
(`spring.modulith.events.republish-outstanding-events-on-restart=true`). Nenhum caso de
uso clínico falha por indisponibilidade do broker.
