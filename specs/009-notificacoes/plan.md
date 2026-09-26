# Plano Técnico — Notificações

- **Spec de origem:** `./spec.md` (Aprovada em 2026-09-25)
- **Módulo Spring Modulith:** `br.com.fiap.sus.notificacoes`
- **Responsável:** Danilo
- **Status:** Aprovado
- **Herda de:** `specs/000-plataforma-sus/plan.md` · **Contrato de eventos:** `specs/000-plataforma-sus/events.md`

---

## 1. Portão constitucional

| Artigo | Verificação | OK? |
|---|---|---|
| I — SDD | Spec aprovada; as duas pendências viraram RN-08 e RN-09 | [x] |
| II — Clean Arch | `domain` sem framework; `NotificacaoRepository` e `EventoProcessadoRepository` são portas | [x] |
| III — Modulith | Sem acesso a internals; sem FK. Depende de `cadastros::api` e de `<modulo>::events` dos seis produtores — exceção registrada na seção 10 | [x] |
| IV — Segurança | `@PreAuthorize("isAuthenticated()")` nos casos de uso; `usuarioId` vem do token; notificação alheia responde 404 | [x] |
| V — Dados | `V0082__not_notificacao.sql` e `V0091__infra_event_publication.sql`; PK UUID; enum como `VARCHAR` com `CHECK` | [x] |
| VI — Eventos | Este módulo só consome; consumidor idempotente por `eventoId`; mensagem sem conteúdo clínico | [x] |
| VII — Testes | Domínio e casos de uso testados sem Spring; meta de 80% no módulo | [x] |
| VIII — API | `/api/v1/notificacoes`; RFC 7807 pelo handler de `shared`; listagem paginada | [x] |
| X — Simplicidade | Sem porta própria para o resolvedor de destinatário (um único implementador); sem consumo Kafka duplicado | [x] |

## 2. Estrutura de pacotes

```
br.com.fiap.sus.notificacoes
├── package-info.java
│     @ApplicationModule(displayName = "Notificacoes", allowedDependencies = {
│         "shared", "cadastros::api",
│         "consultas::events", "exames::events", "resultados::events",
│         "pareceres::events", "receitas::events", "documentos::events" })
├── domain/
│   ├── model/Notificacao.java                       agregado
│   ├── enums/TipoNotificacao.java                   um valor por fato de RF-08
│   └── repository/NotificacaoRepository.java        porta
│                  NotificacaoFiltro.java            (usuarioId, lida opcional, pagina, tamanho)
│                  EventoProcessadoRepository.java   porta de deduplicação
├── application/
│   ├── dto/NotificacaoOutput.java
│   │       ContagemNaoLidasOutput.java
│   │       FatoNotificavel.java                    entrada do processador (eventoId, tipo, referenciaId, destinatários, dados da mensagem)
│   ├── mensagem/CatalogoMensagens.java              título e mensagem por tipo, sem dado clínico
│   └── usecase/ListarMinhasNotificacoesUseCase.java
│               ConsultarMinhaNotificacaoUseCase.java
│               MarcarNotificacaoComoLidaUseCase.java
│               ContarNaoLidasUseCase.java
│               ProcessarFatoUseCase.java            idempotente; um fato → N notificações
├── infrastructure/
│   ├── persistence/entity/NotificacaoEntity.java, EventoProcessadoEntity.java
│   ├── persistence/repository/NotificacaoJpaRepository.java, NotificacaoRepositoryAdapter.java,
│   │                          EventoProcessadoJpaRepository.java, EventoProcessadoRepositoryAdapter.java
│   ├── persistence/mapper/NotificacaoPersistenceMapper.java
│   ├── messaging/NotificacaoEventListener.java      um método @ApplicationModuleListener por evento
│   │            DestinatarioResolver.java           pacienteId/medicoId → usuarioId via CadastroQuery
│   └── config/NotificacoesAsyncConfig.java          @EnableAsync (exigido pelo listener assíncrono)
└── presentation/
    ├── controller/NotificacaoController.java
    └── response/NotificacaoResponse.java, ContagemNaoLidasResponse.java
```

Fora do módulo, mas parte desta feature:

```
br.com.fiap.sus.shared.domain.event.EventoDominio          envelope comum dos eventos
src/main/resources/db/migration/V0091__infra_event_publication.sql
<modulo>.application.event.*Event + package-info.java      nos seis produtores (seção 7)
```

## 3. Modelo de domínio

| Objeto | Tipo | Atributos | Invariantes |
|---|---|---|---|
| `Notificacao` | Entidade | `id`, `usuarioId`, `eventoId`, `tipo`, `titulo` (≤150), `mensagem` (≤500), `referenciaId`, `lida`, `dataLeitura`, `criadoEm` | Nasce não lida (RN-04); `marcarComoLida()` registra `dataLeitura` uma única vez e é idempotente; `usuarioId`, `eventoId`, `tipo`, `titulo` e `mensagem` obrigatórios; `pertenceA(usuarioId)` é a regra de posse |
| `TipoNotificacao` | Enum | `CONSULTA_AGENDADA`, `CONSULTA_REMARCADA`, `CONSULTA_CANCELADA`, `EXAME_SOLICITADO`, `EXAME_AGENDADO`, `RESULTADO_DISPONIVEL`, `PARECER_CRIADO`, `RECEITA_EMITIDA`, `RECEITA_RENOVADA`, `DOCUMENTO_EMITIDO` | Cada valor conhece o **recurso de origem** (`consultas`, `exames`, `resultados`…) para HU-06 |
| `NotificacaoFiltro` | VO | `usuarioId`, `lida` (opcional), `pagina`, `tamanho` | `tamanho` entre 1 e 100 |
| `FatoNotificavel` | DTO de aplicação | `eventoId`, `tipo`, `referenciaId`, `destinatarios` (lista de `Destinatario{usuarioId, papel}`), `parametros` da mensagem | — |

**Mensagens (RN-05):** o `CatalogoMensagens` só interpola identificadores de navegação e
datas. Exemplo: título "Resultado de exame disponível", mensagem "O resultado do seu exame
já está disponível para consulta." Para o médico: "Um resultado de exame que você solicitou
está disponível." Datas em `dd/MM/yyyy HH:mm` no fuso `America/Sao_Paulo`.

## 4. Casos de uso

| Caso de uso | Entrada | Saída | Autorização | Evento |
|---|---|---|---|---|
| `ListarMinhasNotificacoesUseCase` | `lida?`, `pagina`, `tamanho` | `PaginaResultado<NotificacaoOutput>` (mais recente primeiro) | `isAuthenticated()`; `usuarioId` do token | — |
| `ConsultarMinhaNotificacaoUseCase` | `notificacaoId` | `NotificacaoOutput` | `isAuthenticated()`; alheia → `RecursoNaoEncontradoException` | — |
| `MarcarNotificacaoComoLidaUseCase` | `notificacaoId` | `NotificacaoOutput` | `isAuthenticated()`; alheia → 404 (EX-04); já lida → sem efeito | — |
| `ContarNaoLidasUseCase` | — | `ContagemNaoLidasOutput{quantidade}` | `isAuthenticated()` | — |
| `ProcessarFatoUseCase` | `FatoNotificavel` | `int` notificações criadas | interno (sem `@PreAuthorize`; nunca exposto por HTTP) | consome |

**`ProcessarFatoUseCase` (RF-06, RN-03, EX-01, EX-02, EX-05):**

1. Se `EventoProcessadoRepository.jaProcessado(eventoId)` → log de descarte com o `eventoId` e retorna 0.
2. Registra o marcador do evento **na mesma transação** das notificações.
3. Para cada destinatário resolvido, cria uma `Notificacao`. Destinatário sem `usuarioId`
   é descartado com log (RN-10), sem afetar os demais.
4. Violação de chave (`evento_id` duplicado por corrida) é tratada como duplicata, não como erro.

A transação é a do listener (`REQUIRES_NEW`, aberta pelo `@ApplicationModuleListener`).
Se falhar, o Modulith mantém a publicação incompleta e reentrega (EX-05).

## 5. Contrato REST

| Método | Rota | Auth | Request | Response | Códigos |
|---|---|---|---|---|---|
| GET | `/api/v1/notificacoes` | autenticado | query `lida`, `pagina`, `tamanho` | `Pagina<NotificacaoResponse>` | 200, 400, 401 |
| GET | `/api/v1/notificacoes/nao-lidas/contagem` | autenticado | — | `ContagemNaoLidasResponse{quantidade}` | 200, 401 |
| GET | `/api/v1/notificacoes/{id}` | autenticado | — | `NotificacaoResponse` | 200, 401, 404 |
| PATCH | `/api/v1/notificacoes/{id}/leitura` | autenticado | sem corpo | `NotificacaoResponse` | 200, 401, 404 |

`NotificacaoResponse`: `id`, `tipo`, `titulo`, `mensagem`, `referenciaId`, `recurso`
(ex.: `consultas`, para o cliente montar a navegação da HU-06), `lida`, `dataLeitura`,
`criadoEm`. Nunca expõe `usuarioId` nem `eventoId`.

Contrato completo em `./contracts/openapi.yaml`.

## 6. Persistência

### `V0082__not_notificacao.sql`

```sql
CREATE TABLE not_notificacao (
    id             UUID PRIMARY KEY,
    usuario_id     UUID NOT NULL,
    evento_id      UUID NOT NULL,
    tipo           VARCHAR(40) NOT NULL,
    titulo         VARCHAR(150) NOT NULL,
    mensagem       VARCHAR(500) NOT NULL,
    referencia_id  UUID,
    lida           BOOLEAN NOT NULL DEFAULT FALSE,
    data_leitura   TIMESTAMPTZ,
    criado_em      TIMESTAMPTZ NOT NULL,
    atualizado_em  TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_not_tipo CHECK (tipo IN ('CONSULTA_AGENDADA', 'CONSULTA_REMARCADA', 'CONSULTA_CANCELADA',
        'EXAME_SOLICITADO', 'EXAME_AGENDADO', 'RESULTADO_DISPONIVEL', 'PARECER_CRIADO',
        'RECEITA_EMITIDA', 'RECEITA_RENOVADA', 'DOCUMENTO_EMITIDO')),
    CONSTRAINT ck_not_leitura CHECK ((lida = FALSE AND data_leitura IS NULL) OR (lida = TRUE AND data_leitura IS NOT NULL)),
    CONSTRAINT uk_not_evento_usuario UNIQUE (evento_id, usuario_id)
);

CREATE INDEX idx_not_notificacao_usuario_criado ON not_notificacao (usuario_id, criado_em DESC);
CREATE INDEX idx_not_notificacao_usuario_nao_lida ON not_notificacao (usuario_id) WHERE lida = FALSE;

CREATE TABLE not_evento_processado (
    evento_id      UUID PRIMARY KEY,
    tipo_evento    VARCHAR(80) NOT NULL,
    processado_em  TIMESTAMPTZ NOT NULL
);
```

| Tabela | Colunas-chave | Índices | Observações |
|---|---|---|---|
| `not_notificacao` | `usuario_id`, `evento_id` | por usuário + data (listagem); parcial por não lida (contagem barata, RNF) | `uk_not_evento_usuario` garante RN-03 no banco |
| `not_evento_processado` | `evento_id` | PK | marcador de idempotência (Artigo VI.3) |

### `V0091__infra_event_publication.sql`

Tabela `event_publication` do Spring Modulith (starter JDBC), copiada do script
`schema-postgresql.sql` da versão 1.4.3 do artefato `spring-modulith-events-jdbc`.
Com `spring.modulith.events.jdbc.schema-initialization.enabled=false`, o Flyway é a única
fonte do schema (Artigo V.1). Nos testes com H2 a inicialização automática fica ligada.

### Referências entre módulos

| Campo | Módulo dono | Como é validado |
|---|---|---|
| `usuario_id` | `iam` | Resolvido a partir de `CadastroQuery.resumoDoPaciente(..).usuarioId()` / `resumoDoMedico(..).usuarioId()`; sem FK |
| `referencia_id` | módulo do fato | Apenas navegação; sem validação e sem FK |
| `evento_id` | produtor | Chave de deduplicação; sem FK |

## 7. Integração entre módulos

- **Depende de (leitura):** `cadastros::api` → `CadastroQuery` (resolução do destinatário).
- **Publica:** nada.
- **Consome:** os dez eventos do catálogo, cada um por um método
  `@ApplicationModuleListener` em `NotificacaoEventListener`, que monta o `FatoNotificavel`
  e chama `ProcessarFatoUseCase`.

### Mudanças nos produtores (coordenar com Thiago e Juliana)

| Módulo | Mudança |
|---|---|
| `shared` | novo `shared.domain.event.EventoDominio` |
| `consultas` | `application/event/{ConsultaAgendadaEvent, ConsultaRemarcadaEvent, ConsultaCanceladaEvent}` + `package-info` `@NamedInterface("events")`; `@Transactional` e `publishEvent` em `Agendar`, `Remarcar` e `CancelarConsultaUseCase` |
| `exames` | `application/event/{ExameSolicitadoEvent, ExameAgendadoEvent}` + `package-info`; `@Transactional` e `publishEvent` em `Solicitar` e `AgendarExameUseCase`; `ExameQuery.medicoSolicitanteIdDoExame` |
| `resultados` | `application/event/ResultadoExameDisponivelEvent` + `package-info`; `@Transactional` e `publishEvent` nos dois `RegistrarResultado*UseCase` |
| `pareceres` | `ParecerCriadoEvent` ganha `eventoId`, `ocorridoEm`, `implements EventoDominio`, `@Externalized` |
| `receitas` | `application/event/{ReceitaEmitidaEvent, ReceitaRenovadaEvent}` + `package-info`; `@Transactional` e `publishEvent` em `Emitir` e `RenovarReceitaUseCase` |
| `documentos` | `package-info` `@NamedInterface("events")`; `DocumentoEmitidoEvent` ganha `eventoId`, `ocorridoEm`, `EventoDominio`, `@Externalized`; `@Transactional` em `EmitirDocumentoUseCase` |

Os testes unitários existentes desses casos de uso passam a verificar que o evento é
publicado (`ArgumentCaptor` no `ApplicationEventPublisher` mockado).

### Mensageria (Kafka)

| Item | Decisão |
|---|---|
| Dependências | `spring-modulith-starter-jdbc` (registro de publicação), `spring-modulith-events-api` (`@Externalized`), `spring-modulith-events-kafka`, `spring-modulith-events-jackson` |
| Externalização | `spring.modulith.events.externalization.enabled=${MESSAGING_ENABLED:false}`; `docker` liga por padrão |
| Broker | `spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}` |
| Reentrega | `spring.modulith.events.republish-outstanding-events-on-restart=true` |
| Compose | `api` recebe `KAFKA_BOOTSTRAP_SERVERS=kafka:9092` e `MESSAGING_ENABLED=true`; `kafka` ganha healthcheck e `api` depende dele |
| Tópicos | criados automaticamente pelo broker (`auto.create.topics.enable` padrão do `apache/kafka`) |
| Demonstração | `kafka-console-consumer` no container lendo `sus.consultas.agendada.v1` |

## 8. Estratégia de testes

| Nível | Alvo | Ferramenta |
|---|---|---|
| Unitário | `Notificacao` (leitura idempotente, posse, limites), `TipoNotificacao`, `CatalogoMensagens` (nenhuma mensagem com dado clínico) | JUnit 5 + AssertJ |
| Unitário | `ProcessarFatoUseCase` (duplicata, destinatário sem usuário, N destinatários), casos de uso de leitura (posse → 404) | Mockito, sem Spring |
| Integração | adapters com `@DataJpaTest` + H2 e `@Sql` da migration, seguindo o padrão de `pareceres` | Spring Data + H2 |
| Endpoint | `NotificacaoController` com MockMvc standalone e casos de uso mockados; autorização negativa: sem token → 401; notificação de terceiro → 404 | MockMvc + `@EnableMethodSecurity` |
| Módulo | `@ApplicationModuleTest` de `notificacoes` com `Scenario`: publicar `ConsultaAgendadaEvent` → notificação para paciente e médico; publicar de novo o mesmo `eventoId` → nada novo | Spring Modulith Test |
| Arquitetura | `ModularidadeTest` e `ArquiteturaTest` existentes continuam verdes | Modulith + ArchUnit |
| Produtores | teste unitário existente de cada caso de uso passa a exigir o evento publicado | Mockito |

## 9. Riscos e decisões

| Decisão | Alternativa descartada | Motivo |
|---|---|---|
| Notificação gerada pelo listener em memória; Kafka só externaliza | Consumir do Kafka com `@KafkaListener` para gerar a notificação | Dois caminhos para o mesmo efeito; sem Kafka o segundo não existe. A idempotência já é exigida pelo registro de publicação |
| Manter `ParecerCriadoEvent` e `DocumentoEmitidoEvent` com os nomes atuais | Renomear para os nomes do catálogo original | Renomear classe de outro módulo sem ganho funcional |
| `evento_id` também em `not_notificacao` com `UNIQUE (evento_id, usuario_id)` | Apenas `not_evento_processado` | Garante RN-03 no banco e dá rastreabilidade fato → notificação |
| Eventos em `application.event` exportado como `events` | `domain.event` (template) | `@Externalized` é anotação Spring; `domain` é livre de framework |
| Chave Kafka = `pacienteId` | Sem chave | Ordem por paciente sem custo |
| H2 nos testes de persistência | Testcontainers | Padrão já adotado por todos os módulos; Docker não é pré-requisito do `mvn verify` |
| **Risco:** `@ApplicationModuleTest` com H2 e registro JDBC de eventos | — | Se o schema automático do Modulith não subir no H2, o teste de módulo cai para `@SpringBootTest` mínimo com `spring.modulith.events.jdbc.schema-initialization.enabled=true`; registrar no `tasks.md` |
| **Risco:** `@EnableAsync` global | — | Habilitar em config do módulo `notificacoes`; verificar que nenhum outro bean passa a ser assíncrono sem querer |

## 10. Complexity Tracking (exceções à constituição)

| Artigo | Exceção | Por quê | Alternativa rejeitada |
|---|---|---|---|
| III (plano-mãe: `notificacoes → shared`) | Depende também de `cadastros::api` e de `<modulo>::events` dos seis produtores | O destinatário é um usuário e o evento carrega `pacienteId`/`medicoId`; o tipo do evento precisa ser visível para o listener | Carregar `usuarioId` no evento: obrigaria todo produtor a consultar `cadastros` a mais e acopla o payload à necessidade de um consumidor |
| V.2 (faixa `V0080–V0089` de notificações) | Usa `V0082` porque `documentos` ocupou `V0080` e `V0081` | Migration aplicada não se edita | Renumerar as de `documentos` |
| VII.1 (Testcontainers) | H2 em `@DataJpaTest` | Prática já vigente nos oito módulos; `mvn verify` roda sem Docker | Testcontainers só neste módulo: inconsistência e build mais lento |
