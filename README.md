# Plataforma Digital de Atendimento SUS

Hackathon FIAP — Grupo 32 · Luis · Thiago · Juliana · Gilmar · Danilo

API que centraliza o ciclo de atendimento do paciente do SUS: agendamento de consultas,
solicitação e realização de exames, resultados, parecer médico, receitas, documentos e
notificações — com acesso controlado por perfil.

**Princípio central:** o dado técnico do exame é separado da interpretação clínica do
médico. O resultado é persistido de forma independente do parecer, preservando autoria,
histórico e rastreabilidade.

## Vídeos

| Vídeo | YouTube | Arquivo local |
|---|---|---|
| Pitch de apresentação | [youtube.com/watch?v=dUrBcF15nv8](https://www.youtube.com/watch?v=dUrBcF15nv8) | [`pitch_Hackaton.mp4`](pitch_Hackaton.mp4) |
| Demonstração do MVP | [youtu.be/WIOXiWBTkg0](https://youtu.be/WIOXiWBTkg0) | [`23-46-03.mp4`](23-46-03.mp4) |

## Estado atual

| Módulo | Feature | Status |
|---|---|---|
| `shared` | tipos comuns, erro RFC 7807, contexto de segurança | ✅ implementado |
| `iam` | [001 — usuários, autenticação e perfis](specs/001-iam-seguranca/spec.md) | ✅ implementado |
| `cadastros` | [002 — paciente, médico e catálogos](specs/002-cadastros/spec.md) | ✅ implementado |
| `consultas` | [003](specs/003-consultas/spec.md) | ✅ implementado (Thiago) |
| `exames` · `resultados` | [004](specs/004-exames/spec.md) · [005](specs/005-resultados/spec.md) | ✅ implementado (Thiago) |
| `pareceres` · `documentos` | [006](specs/006-pareceres/spec.md) · [008](specs/008-documentos/spec.md) | ✅ implementado (Juliana) |
| `receitas` | [007](specs/007-receitas/spec.md) | ✅ implementado (Thiago) |
| `notificacoes` | [009](specs/009-notificacoes/spec.md) | ✅ implementado (Danilo) — eventos de domínio + Kafka |
| `historico` | [010](specs/010-historico/spec.md) | ✅ implementado (Danilo) — linha do tempo derivada, sem tabela |

Build: **422 testes passando** (`mvn verify -Pquality`), incluindo verificação de fronteiras do
Spring Modulith, regra de dependência com ArchUnit, teste de módulo com eventos e gate de
cobertura de 80%.

## Como rodar

### Com Docker (recomendado)

```bash
cp .env.example .env      # opcional: ajuste segredo e credenciais
docker compose up --build # sobe Postgres, Kafka, API, Prometheus e Grafana
```

Se já houver um Postgres na 5432 na sua máquina, use outra porta:
`DB_PORT=5433 docker compose up --build` (para rodar a API fora do Compose nesse caso,
aponte `DB_URL=jdbc:postgresql://localhost:5433/sus`).

### Local (precisa de um Postgres na porta 5432)

```bash
mvn spring-boot:run
```

Na primeira subida o Flyway cria o schema, carrega os quatro perfis e os catálogos de
demonstração, e a aplicação cria o administrador inicial:

```
admin@sus.gov.br  /  Admin@123
```

Troque essa senha antes de qualquer uso real; desligue o seed com `SEED_ENABLED=false`.

## Documentação da API

- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

Autentique em `POST /api/v1/auth/login`, copie o campo `token` e cole no botão
**Authorize** do Swagger (o prefixo `Bearer` é adicionado automaticamente).

## Observabilidade (Prometheus + Grafana)

A stack local sobe com métricas da API via Spring Boot Actuator + Micrometer, coleta pelo
Prometheus e visualização no Grafana.

Arquivos envolvidos:

- [`pom.xml`](pom.xml): inclui `spring-boot-starter-actuator` e `micrometer-registry-prometheus`.
- [`src/main/resources/application.yml`](src/main/resources/application.yml): expõe `health`, `info`
  e `prometheus`, com tag `application=sus-plataforma`.
- [`prometheus.yml`](prometheus.yml): coleta `api:8080/actuator/prometheus`.
- [`grafana/provisioning/datasources/prometheus.yml`](grafana/provisioning/datasources/prometheus.yml):
  registra o datasource Prometheus automaticamente.
- [`grafana/provisioning/dashboards/dashboard.yml`](grafana/provisioning/dashboards/dashboard.yml):
  registra a pasta de dashboards.
- [`grafana/dashboards/sus-api-overview.json`](grafana/dashboards/sus-api-overview.json):
  dashboard inicial da API.

### URLs

- **Aplicação API:** http://localhost:8080
- **Actuator Health:** http://localhost:8080/actuator/health
- **Métricas Prometheus da API:** http://localhost:8080/actuator/prometheus
- **Prometheus:** http://localhost:9090
- **Grafana:** http://localhost:3000

### Passo a passo

1. Suba a stack:
   ```bash
   docker compose up -d --build
   ```

2. Confira os containers:
   ```bash
   docker compose ps
   ```
   Esperado: `sus-api`, `sus-postgres`, `sus-kafka`, `sus-prometheus` e `sus-grafana`
   ativos.

3. Valide a API:
   - Health: http://localhost:8080/actuator/health
   - Métricas: http://localhost:8080/actuator/prometheus
   - A página de métricas deve aparecer em texto, com linhas como
     `jvm_memory_used_bytes` e `http_server_requests_seconds_count`.

4. Valide o Prometheus:
   - Abra http://localhost:9090/targets
   - O job `sus-api` deve aparecer como `UP`.
   - O endpoint coletado deve ser `http://api:8080/actuator/prometheus`.
   - Se aparecer `DOWN`, confira o [`prometheus.yml`](prometheus.yml): dentro do Docker,
     use o nome do serviço (`api:8080`), não `localhost`.

5. Valide o Grafana:
   - Abra http://localhost:3000
   - Login: `admin`
   - Senha: `admin`
   - No primeiro acesso, o Grafana pode pedir a troca da senha.
   - Acesse **Dashboards → SUS API Overview** para ver o dashboard provisionado pelo projeto.

### Como acessar o Prometheus

1. Abra http://localhost:8080/actuator/prometheus no navegador.
2. Confirme que a API expõe métricas em texto. Exemplos esperados:
   - `jvm_memory_used_bytes`
   - `http_server_requests_seconds_count`
3. Abra http://localhost:9090/targets.
4. Confirme que o job **sus-api** aparece como **UP**.
5. Confirme que o endpoint mostrado é `http://api:8080/actuator/prometheus`.

Se o target `sus-api` aparecer como `DOWN`, confira:

- Se a API está rodando: http://localhost:8080/actuator/health
- Se o [`prometheus.yml`](prometheus.yml) usa `api:8080`, que é o nome do serviço dentro
  do Docker Compose.
- Os logs:
  ```bash
  docker compose logs -f api
  docker compose logs -f prometheus
  ```

### Como acessar o Grafana

1. Abra http://localhost:3000
2. Faça login com:
   - **Usuário:** `admin`
   - **Senha:** `admin`
3. Se o Grafana pedir, troque a senha no primeiro acesso.
4. O projeto já provisiona um datasource e um dashboard inicial:
   - **Dashboards → SUS API Overview**
5. Para consultas manuais, vá em **Explore**, selecione **Prometheus** e teste:
   ```promql
   rate(http_server_requests_seconds_count{application="sus-plataforma"}[5m])
   ```
6. Se o dashboard ou datasource não aparecer automaticamente, reinicie o Grafana:
   ```bash
   docker compose restart grafana
   ```

### Como configurar o Prometheus no Grafana manualmente

Use estes passos se o datasource não aparecer automaticamente ou se você quiser configurar
um datasource novo:

1. Abra http://localhost:3000.
2. Faça login com `admin` / `admin` ou com a senha que você definiu no primeiro acesso.
3. No menu lateral, acesse **Connections → Data sources**.
4. Clique em **Add data source**.
5. Escolha **Prometheus**.
6. Em **Connection → Prometheus server URL**, informe:
   ```text
   http://prometheus:9090
   ```
   Esse é o nome do serviço do Prometheus dentro do `docker-compose.yml`. No Grafana em
   container, use `prometheus:9090`, não `localhost:9090`.
7. Role até o final e clique em **Save & test**.
8. A mensagem esperada é **Successfully queried the Prometheus API**.

### Como importar dashboards prontos no Grafana

Além do dashboard inicial do projeto, você pode importar dashboards públicos:

1. No Grafana, acesse **Dashboards → New → Import**.
2. Em **Import via grafana.com**, informe um dos IDs:

   | ID | Dashboard |
   |---:|---|
   | `19004` | Spring Boot 3 Statistics |
   | `4701` | JVM (Micrometer): memória, threads, GC |

3. Clique em **Load**.
4. No campo **Prometheus**, selecione o datasource criado ou provisionado.
5. Clique em **Import**.
6. No topo do dashboard, se houver filtro, selecione `application = sus-plataforma`.
7. Ajuste o período para **Last 15 minutes**.
8. Gere tráfego na API para alimentar os painéis:
   - Swagger: http://localhost:8080/swagger-ui.html
   - Swagger alternativo: http://localhost:8080/swagger-ui/index.html
   - Postman: rode algumas requisições da collection em [`postman/`](postman/).

### Verificação rápida de funcionamento

- API respondendo: http://localhost:8080/actuator/health
- Coleta do Prometheus: http://localhost:9090/targets
- Dashboard Grafana: http://localhost:3000

### Exemplos de query no Grafana

#### Requests por segundo

```promql
rate(http_server_requests_seconds_count{application="sus-plataforma"}[5m])
```

#### Tempo de resposta por endpoint

```promql
histogram_quantile(0.95, sum by (le, uri) (rate(http_server_requests_seconds_bucket{application="sus-plataforma"}[5m])))
```

#### Uso de memória JVM

```promql
jvm_memory_used_bytes{application="sus-plataforma"}
```

#### Health da aplicação

```promql
up{job="sus-api"}
```

Se alguma das URLs não responder, rode:

```bash
docker compose up -d --build
```

E confirme se os containers estão ativos:

```bash
docker compose ps
```

Se o target `sus-api` aparecer como `DOWN` no Prometheus:

1. Veja os logs da API:
   ```bash
   docker compose logs -f api
   ```
2. Veja os logs do Prometheus:
   ```bash
   docker compose logs -f prometheus
   ```
3. Confirme se a API responde dentro da stack:
   ```bash
   docker compose exec prometheus wget -qO- http://api:8080/actuator/health
   ```

## Postman

Em [`postman/`](postman/):

- `SUS-Plataforma.postman_collection.json` — coleção completa
- `SUS-Local.postman_environment.json` — environment apontando para `localhost:8080`

Importe os dois no Postman, selecione o environment **SUS - Local** e rode
**1. Autenticação › Login (administrador)**: o token é salvo automaticamente e usado pelas
demais requisições. Rode as pastas na ordem numérica — cada uma guarda os ids que a
seguinte utiliza.

Ao copiar uma chamada como `curl`, substitua variáveis como `{{pacienteId}}`,
`{{tokenMedico}}` e `{{tipoExameImagemId}}` pelos valores reais salvos no Postman.
Se o backend receber `{{pacienteId}}` literalmente, ele responde `400` porque esse texto
não pode ser convertido para `UUID`.

A pasta **14. Notificações** lista, conta e marca como lidas as notificações geradas pelas pastas
anteriores (consulta agendada, exame solicitado, resultado, parecer, documento), para o paciente
e para o médico.

A pasta **15. Testes de segurança** é executável e demonstra o controle de acesso:
`401` sem token, `403` por perfil insuficiente e `404` quando um paciente tenta ler o
cadastro de outro.

A pasta **16. Histórico do paciente** monta a linha do tempo a partir de tudo o que as pastas
anteriores criaram, como paciente, como médico e como administrador, e mostra os erros de
acesso (`400` sem `pacienteId`, `404` para paciente que o médico não atende).

## Mensageria e notificações

Cada caso de uso clínico publica um **evento de domínio** dentro da própria transação
(`ConsultaAgendadaEvent`, `ResultadoExameDisponivelEvent`, `ReceitaEmitidaEvent`…). O Spring
Modulith grava a publicação na tabela `event_publication` e, após o commit, entrega o evento ao
módulo `notificacoes`, que cria uma notificação por destinatário (paciente e, quando faz
sentido, médico), deduplicada pelo `eventoId`. A mensagem nunca carrega dado clínico.

Com `MESSAGING_ENABLED=true` (padrão no Compose) o mesmo evento é **externalizado para o
Kafka** no tópico `sus.<modulo>.<evento>.v1`, com o `pacienteId` como chave. Sem Kafka
(`MESSAGING_ENABLED=false`, padrão no perfil `local`) a aplicação sobe, o fluxo clínico
funciona e as notificações continuam sendo geradas; só a externalização é desligada.

Para observar os eventos chegando ao broker:

```bash
docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic sus.consultas.agendada.v1 --from-beginning --property print.key=true
```

Catálogo completo em [`specs/000-plataforma-sus/events.md`](specs/000-plataforma-sus/events.md)
e guia de demonstração em [`docs/mensageria-e-notificacoes.md`](docs/mensageria-e-notificacoes.md).

## Histórico do paciente

`GET /api/v1/historico` devolve a **linha do tempo** do atendimento: consultas, solicitações de
exame, exames, resultados, pareceres, receitas e documentos em uma única lista, do registro mais
recente para o mais antigo, com filtro por `tipo` (repetível) e por período (`inicio`, `fim`).
Não existe tabela de histórico: o módulo `historico` lê, em tempo de requisição, a porta `api`
de cada módulo clínico e ordena em memória. Cada item traz `tipo`, `recurso`, `id`, `data`,
`titulo`, `situacao`, `medicoId` e `origem` (o registro relacionado), nunca o conteúdo clínico;
o detalhe é lido na rota do módulo dono, com a regra de acesso daquele módulo.

| Perfil | O que vê |
|---|---|
| `PACIENTE` | O próprio histórico; `pacienteId` da requisição é ignorado |
| `MEDICO` | Pacientes com quem tem consulta não cancelada (`pacienteId` obrigatório); outro paciente responde `404` |
| `ADMINISTRADOR` | Qualquer paciente existente (`pacienteId` obrigatório) |
| `ATENDENTE` | Não acessa (`403`) |

```bash
curl -s http://localhost:8080/api/v1/historico?tipo=CONSULTA&tipo=RESULTADO_EXAME -H "Authorization: Bearer <token do paciente>"
```

Spec, plano e contrato em [`specs/010-historico/`](specs/010-historico/).

## Arquitetura

Monólito modular com **Spring Modulith**, onde cada módulo de negócio é uma
"microaplicação" com **Clean Architecture** própria, fronteira verificada em tempo de build
e comunicação por porta publicada (leitura síncrona) ou evento de domínio (reação
assíncrona via Kafka).

```
presentation → application → domain ← infrastructure
```

O `domain` é Java puro — sem Spring, sem JPA. Entidade de domínio não é entidade JPA.
Módulos se referenciam por `UUID` validado através da porta pública do módulo dono; não há
FK entre tabelas de módulos diferentes, o que mantém cada módulo extraível.

Stack: Java 21 · Spring Boot 3.5 · Spring Security (JWT) · Spring Data JPA · PostgreSQL 16
· Flyway · Apache Kafka · Docker Compose · JUnit 5 + Mockito + ArchUnit · springdoc-openapi.

## Como navegar neste repositório

| Onde | O que é |
|---|---|
| [`.specify/memory/constitution.md`](.specify/memory/constitution.md) | Regras não negociáveis do projeto |
| [`specs/README.md`](specs/README.md) | Índice de todas as especificações |
| [`specs/000-plataforma-sus/`](specs/000-plataforma-sus/) | Spec do produto, plano de arquitetura, modelo de dados, eventos |
| [`specs/NNN-*/`](specs/) | Uma pasta por feature: `spec.md`, `plan.md`, `tasks.md` |
| [`src/main/java/br/com/fiap/sus/`](src/main/java/br/com/fiap/sus/) | Um pacote por módulo |
| [`src/main/resources/db/migration/`](src/main/resources/db/migration/) | Migrations Flyway, faixa numérica reservada por módulo |
| [`postman/`](postman/) | Coleção e environment |
| [`docs/fluxo-sdd.md`](docs/fluxo-sdd.md) | Como a equipe trabalha no dia a dia |
| [`CLAUDE.md`](CLAUDE.md) | Guia de contexto para quem (ou o que) chega agora |

## Desenvolvimento

```bash
mvn verify              # compila, roda os testes, valida fronteiras de módulo
mvn verify -Pquality    # roda a suíte e exige cobertura de linhas mínima de 80% (atual: 80,4%)
```

Fluxo de trabalho (Spec-Driven Development):

```
/specify <feature>  →  /plan <NNN>  →  /tasks <NNN>  →  /implement <NNN>  →  /analyze <NNN>
```

Nenhum código de produção antes da spec aprovada. Detalhes em
[`docs/fluxo-sdd.md`](docs/fluxo-sdd.md).

## Dívida conhecida

- O gate de cobertura de 80% já passa no profile `quality`; manter novos módulos cobertos para não
  regredir esse número.
- Pendências `[NEEDS CLARIFICATION]` das features `003` a `008` continuam abertas na spec-mãe.
