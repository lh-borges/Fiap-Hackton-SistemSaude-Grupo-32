# Mensageria e notificações — guia de demonstração

Feature `009-notificacoes` · Responsável: Danilo · Spec em `specs/009-notificacoes/`.

## 1. O que acontece quando um fato clínico ocorre

```
caso de uso (@Transactional)
   │  1. grava o agregado (consulta, exame, resultado, receita…)
   │  2. publishEvent(<Fato>Event)          ← dentro da transação
   ▼
Spring Modulith — event_publication         ← publicação persistida junto com o commit
   │
   ├──► notificacoes.NotificacaoEventListener   (após o commit, assíncrono, transação própria)
   │        ├─ DestinatarioResolver: pacienteId/medicoId → usuarioId (porta cadastros::api)
   │        └─ ProcessarFatoUseCase: dedup por eventoId → 1 notificação por destinatário
   │
   └──► Kafka  sus.<modulo>.<evento>.v1      (só com MESSAGING_ENABLED=true; chave = pacienteId)
```

Três garantias que a constituição exige e a implementação cumpre:

| Garantia | Como |
|---|---|
| O fluxo clínico não depende do broker (Artigo VI.4, HU-05) | A notificação nasce do listener em memória; o Kafka só recebe a externalização. Sem Kafka a API sobe e notifica. |
| Reentrega não duplica (Artigo VI.3, HU-04) | `not_evento_processado` + `UNIQUE (evento_id, usuario_id)` em `not_notificacao`. |
| Nada clínico no evento nem na mensagem (Artigo VI.2, RN-05) | Payload só com IDs, datas e contagens; catálogo de mensagens fixo e testado. |

## 2. Subindo o ambiente

```bash
docker compose up --build
```

Sobe Postgres, Kafka (KRaft), a API, Prometheus e Grafana. A API espera o Postgres e o Kafka
ficarem saudáveis. Variáveis relevantes (ver `.env.example`):

| Variável | Padrão no Compose | Efeito |
|---|---|---|
| `MESSAGING_ENABLED` | `true` | Liga a externalização dos eventos para o Kafka |
| `KAFKA_BOOTSTRAP_SERVERS` | `kafka:9092` | Broker usado pela API |

Para demonstrar a degradação sem broker: `MESSAGING_ENABLED=false docker compose up --build`.
As notificações continuam aparecendo na API; os tópicos ficam vazios.

## 3. Roteiro de demonstração (Postman)

1. **1. Autenticação** → login do administrador.
2. **3. Pacientes**, **4. Médicos** → cadastrar e logar paciente e médico (a coleção guarda os tokens).
3. **9. Consultas → Agendar consulta** → evento `ConsultaAgendadaEvent`.
4. **14. Notificações → Listar minhas notificações (paciente)** → aparece "Consulta agendada",
   não lida, com `referenciaId` da consulta e `recurso = consultas`.
5. **14. Notificações → Listar minhas notificações (médico)** → "Nova consulta na sua agenda".
6. **Marcar como lida** → `lida = true`, `dataLeitura` preenchida. Repetir → mesma data.
7. **Seguranca: admin não lê notificação do paciente** → `404`.
8. Seguir o fluxo clínico (solicitar exame, agendar, registrar resultado, parecer, documento)
   e observar a lista do paciente crescer: um aviso por fato, sem conteúdo clínico.

## 4. Observando o Kafka

Consumidor no próprio container do broker:

```bash
docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic sus.consultas.agendada.v1 --from-beginning --property print.key=true
```

> No Git Bash do Windows, prefixe os comandos `docker compose exec` com `MSYS_NO_PATHCONV=1`,
> senão o caminho `/opt/kafka/...` é convertido para um caminho do Windows.

Listar os tópicos criados pela externalização:

```bash
docker compose exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```

Registro de publicações do Modulith (mostra o que foi entregue e quando):

```bash
docker compose exec postgres psql -U sus -d sus -c "select listener_id, event_type, publication_date, completion_date from event_publication order by publication_date desc limit 10"
```

Exemplo real de mensagem no tópico `sus.consultas.agendada.v1` (chave = `pacienteId`,
payload só com identificadores e datas):

```
7c8a500f-… => {"eventoId":"c910933f-…","ocorridoEm":"2026-09-26T01:16:14Z","consultaId":"56f4eee8-…","pacienteId":"7c8a500f-…","medicoId":"c4971ef3-…","unidadeSaudeId":"00000000-0000-0000-0000-0000000000b1","dataHora":"2036-03-20T12:30:00Z"}
```

## 5. Catálogo resumido

| Evento | Tópico | Notifica |
|---|---|---|
| `ConsultaAgendadaEvent` / `ConsultaRemarcadaEvent` / `ConsultaCanceladaEvent` | `sus.consultas.{agendada,remarcada,cancelada}.v1` | paciente e médico |
| `ExameSolicitadoEvent` / `ExameAgendadoEvent` | `sus.exames.{solicitado,agendado}.v1` | paciente |
| `ResultadoExameDisponivelEvent` | `sus.resultados.disponivel.v1` | paciente e médico solicitante |
| `ParecerCriadoEvent` | `sus.pareceres.criado.v1` | paciente |
| `ReceitaEmitidaEvent` / `ReceitaRenovadaEvent` | `sus.receitas.{emitida,renovada}.v1` | paciente |
| `DocumentoEmitidoEvent` | `sus.documentos.emitido.v1` | paciente |

Contrato completo: `specs/000-plataforma-sus/events.md`.

## 6. API de notificações

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/v1/notificacoes?lida=false&pagina=0&tamanho=20` | Minhas notificações, da mais recente para a mais antiga |
| GET | `/api/v1/notificacoes/nao-lidas/contagem` | Quantidade de não lidas |
| GET | `/api/v1/notificacoes/{id}` | Detalhe (404 se for de outro usuário) |
| PATCH | `/api/v1/notificacoes/{id}/leitura` | Marca como lida (irreversível, idempotente) |

Contrato OpenAPI: `specs/009-notificacoes/contracts/openapi.yaml`; Swagger em `/swagger-ui.html`.

## 7. Roteiro para o vídeo (trecho de mensageria, ~3 min)

1. **Contexto (20 s):** o paciente só sabia do resultado se alguém ligasse. A plataforma avisa sozinha, sem acoplar o aviso ao fluxo clínico.
2. **Arquitetura (40 s):** mostrar o diagrama da seção 1. Evento publicado na transação; Modulith entrega após o commit; notificações e Kafka são consumidores independentes.
3. **Demo (90 s):** agendar consulta no Postman → listar notificações do paciente e do médico → console consumer do Kafka mostrando a mensagem com a chave `pacienteId`.
4. **Resiliência (30 s):** subir com `MESSAGING_ENABLED=false` e mostrar que a consulta é agendada e a notificação aparece mesmo assim. Republicar o mesmo evento (teste de módulo) não duplica.
5. **Fechamento (10 s):** dez fatos cobertos, mensagem sem dado clínico, 404 para notificação de terceiro.
