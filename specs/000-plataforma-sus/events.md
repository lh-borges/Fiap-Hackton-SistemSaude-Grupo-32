# Catálogo de Eventos

Contrato entre os módulos produtores e o módulo `notificacoes`. Este arquivo é a fonte de
verdade: `009-notificacoes` pode ser desenvolvido contra ele antes dos produtores existirem.

## Regras

1. Nome da classe: `<Agregado><FatoNoPassado>Event`, em `<modulo>.domain.event`.
2. `record` imutável, com `eventoId` (UUID), `ocorridoEm` (Instant) e os IDs necessários.
3. **Payload não carrega conteúdo clínico** — sem laudo, sem valor de exame, sem
   diagnóstico, sem medicamento. Só identificadores e metadados de navegação.
4. Publicado pelo caso de uso dentro da transação; entregue após o commit.
5. Externalizado para Kafka com `@Externalized("sus.<modulo>.<evento>.v1")`.
6. Consumidor é idempotente por `eventoId` (tabela `not_evento_processado`).
7. Evolução: campo novo é opcional; mudança incompatível cria a versão `.v2` do tópico.

## Envelope comum

```java
public interface EventoDominio {
    UUID eventoId();
    Instant ocorridoEm();
}
```

## Eventos

| Evento | Produtor | Tópico | Payload | Notificação gerada para |
|---|---|---|---|---|
| `ConsultaAgendadaEvent` | `consultas` | `sus.consultas.agendada.v1` | `consultaId`, `pacienteId`, `medicoId`, `unidadeSaudeId`, `dataHora` | paciente e médico |
| `ConsultaRemarcadaEvent` | `consultas` | `sus.consultas.remarcada.v1` | `consultaId`, `pacienteId`, `medicoId`, `dataHoraAnterior`, `dataHoraNova` | paciente e médico |
| `ConsultaCanceladaEvent` | `consultas` | `sus.consultas.cancelada.v1` | `consultaId`, `pacienteId`, `medicoId`, `dataHora`, `canceladaPorUsuarioId` | paciente e médico |
| `ExameSolicitadoEvent` | `exames` | `sus.exames.solicitado.v1` | `solicitacaoId`, `pacienteId`, `medicoId`, `tipoExameId` | paciente |
| `ExameAgendadoEvent` | `exames` | `sus.exames.agendado.v1` | `exameId`, `solicitacaoId`, `pacienteId`, `unidadeSaudeId`, `dataAgendada` | paciente |
| `ExameRealizadoEvent` | `exames` | `sus.exames.realizado.v1` | `exameId`, `pacienteId`, `dataRealizacao` | — (gatilho interno) |
| `ResultadoExameDisponivelEvent` | `resultados` | `sus.resultados.disponivel.v1` | `resultadoId`, `exameId`, `pacienteId`, `medicoSolicitanteId`, `tipoResultado` | paciente e médico solicitante |
| `ParecerMedicoCriadoEvent` | `pareceres` | `sus.pareceres.criado.v1` | `parecerId`, `resultadoId`, `pacienteId`, `medicoId` | paciente |
| `ReceitaEmitidaEvent` | `receitas` | `sus.receitas.emitida.v1` | `receitaId`, `pacienteId`, `medicoId`, `validade`, `quantidadeItens` | paciente |
| `ReceitaRenovadaEvent` | `receitas` | `sus.receitas.renovada.v1` | `receitaId`, `receitaOrigemId`, `pacienteId`, `medicoId` | paciente |
| `DocumentoMedicoEmitidoEvent` | `documentos` | `sus.documentos.emitido.v1` | `documentoId`, `pacienteId`, `medicoId`, `tipoDocumento` | paciente |

## Exemplo

```java
@Externalized("sus.resultados.disponivel.v1")
public record ResultadoExameDisponivelEvent(
        UUID eventoId,
        Instant ocorridoEm,
        UUID resultadoId,
        UUID exameId,
        UUID pacienteId,
        UUID medicoSolicitanteId,
        TipoResultado tipoResultado
) implements EventoDominio {

    public static ResultadoExameDisponivelEvent de(ResultadoExame r, UUID medicoSolicitanteId) {
        return new ResultadoExameDisponivelEvent(
                UUID.randomUUID(), Instant.now(),
                r.getId(), r.getExameId(), r.getPacienteId(),
                medicoSolicitanteId, r.getTipoResultado());
    }
}
```

## Degradação sem Kafka

Com `sus.messaging.enabled=false` (ou perfil `local`), a externalização é desligada e o
`@ApplicationModuleListener` de `notificacoes` continua recebendo o evento em memória após
o commit. O fluxo clínico e as notificações continuam funcionando; apenas a integração
externa fica indisponível. Nenhum caso de uso clínico pode falhar por indisponibilidade do
broker.
