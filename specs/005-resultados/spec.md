# Especificação — Resultados de exame (imagem e laboratorial)

- **ID:** `005-resultados` · **Módulo:** `resultados` · **Responsável:** Thiago
- **Status:** Em revisão · **Criada em:** 2026-09-03
- **Herda de:** `specs/000-plataforma-sus/spec.md`

## 1. Contexto e problema

O resultado é o dado técnico produzido pela realização do exame. Ele precisa existir e ser
consultável **independentemente** de haver interpretação médica — é isso que permite ao
paciente acompanhar e ao médico analisar remotamente. Resultado laboratorial guardado como
texto solto perde comparabilidade; por isso cada parâmetro é registrado de forma
estruturada.

## 2. Objetivo

Registrar e disponibilizar o resultado técnico de um exame realizado, em formato de imagem
ou em parâmetros laboratoriais estruturados, com faixa de referência e situação de cada
parâmetro.

## 3. Fora de escopo

- Interpretação clínica (é `006-pareceres`).
- Visualizador DICOM e manipulação de imagem.
- Integração automática com equipamento ou laboratório.
- Comparação histórica e gráfico de evolução de parâmetro.

## 4. Atores e permissões

| Ator | O que pode fazer |
|---|---|
| ADMINISTRADOR | Ver todos os resultados; registrar resultado |
| ATENDENTE | Registrar o resultado de um exame realizado na sua unidade |
| MEDICO | Consultar resultados dos pacientes que atende |
| PACIENTE | Consultar os próprios resultados |

## 5. Histórias de usuário

### HU-01 — Registrar resultado de imagem
```gherkin
Dado que existe um exame realizado de categoria imagem
Quando registro o resultado com referência do arquivo, descrição e laudo técnico
Então o resultado é criado e vinculado àquele exame
E o paciente e o médico solicitante são notificados de que há resultado disponível
```

### HU-02 — Registrar resultado laboratorial
```gherkin
Dado que existe um exame realizado de categoria laboratorial
Quando registro o resultado com um ou mais parâmetros
Então cada parâmetro guarda nome, valor ou texto, unidade, faixa de referência e situação
E o resultado fica disponível para consulta

Quando nenhum parâmetro é informado
Então o registro é rejeitado
```

### HU-03 — Situação do parâmetro
```gherkin
Dado um parâmetro quantitativo com faixa de referência informada
Quando o valor está dentro da faixa
Então a situação é "normal"
Quando o valor está abaixo do mínimo
Então a situação é "abaixo da referência"
Quando o valor está acima do máximo
Então a situação é "acima da referência"

Dado um parâmetro qualitativo (por exemplo "não reagente")
Então não se aplica faixa numérica e a situação é informada explicitamente
```

### HU-04 — Um resultado por exame
```gherkin
Dado que um exame já possui resultado registrado
Quando se tenta registrar outro resultado para o mesmo exame
Então a operação é rejeitada
```

### HU-05 — Consulta pelo paciente
```gherkin
Dado que sou paciente autenticado
Quando consulto um resultado meu
Então vejo os parâmetros, valores, faixas de referência e situações
E vejo se já existe parecer médico associado
Quando tento consultar resultado de outro paciente
Então o registro é tratado como inexistente
```

### HU-06 — Imutabilidade
```gherkin
Dado que existe um resultado registrado
Quando um parecer médico é criado sobre ele
Então o resultado permanece exatamente como foi registrado
```

## 6. Requisitos funcionais

| ID | Requisito | Prioridade |
|---|---|---|
| RF-01 | O sistema DEVE permitir registrar resultado de imagem para um exame realizado | Obrigatório |
| RF-02 | O sistema DEVE permitir registrar resultado laboratorial com múltiplos parâmetros | Obrigatório |
| RF-03 | O sistema DEVE registrar, por parâmetro, nome, valor ou texto, unidade, faixa de referência e situação | Obrigatório |
| RF-04 | O sistema DEVE calcular a situação do parâmetro quando houver valor e faixa numérica | Obrigatório |
| RF-05 | O sistema DEVE aceitar resultado não numérico (positivo, negativo, não reagente, tipo sanguíneo) | Obrigatório |
| RF-06 | O sistema DEVE permitir no máximo um resultado por exame | Obrigatório |
| RF-07 | O sistema DEVE listar resultados por paciente e por período, restrito ao titular | Obrigatório |
| RF-08 | O sistema DEVE notificar paciente e médico solicitante quando o resultado ficar disponível | Obrigatório |
| RF-09 | O sistema DEVE informar a outros módulos a existência e o titular de um resultado | Obrigatório |

## 7. Regras de negócio

| ID | Regra |
|---|---|
| RN-01 | Resultado só é registrado para exame com situação "realizado" |
| RN-02 | O tipo do resultado deve corresponder à categoria do tipo de exame solicitado |
| RN-03 | Um exame tem no máximo um resultado |
| RN-04 | Um resultado é de imagem **ou** laboratorial, nunca dos dois |
| RN-05 | Resultado laboratorial tem ao menos um parâmetro |
| RN-06 | Cada parâmetro tem valor numérico com unidade **ou** resultado em texto; nunca ambos vazios |
| RN-07 | Faixa de referência só se aplica a parâmetro numérico; o mínimo não pode ser maior que o máximo |
| RN-08 | Resultado registrado é imutável; correção gera retificação registrada, nunca sobrescrita silenciosa |
| RN-09 | O parecer médico não altera o resultado |

## 8. Fluxos de exceção

| ID | Situação | Comportamento |
|---|---|---|
| EX-01 | Exame não realizado | Rejeitar informando a situação atual |
| EX-02 | Segundo resultado para o mesmo exame | Rejeitar por conflito |
| EX-03 | Tipo de resultado incompatível com o tipo de exame | Rejeitar |
| EX-04 | Parâmetro sem valor e sem texto | Rejeitar apontando o parâmetro |
| EX-05 | Faixa de referência invertida | Rejeitar |
| EX-06 | Paciente acessa resultado de terceiro | Tratar como inexistente |

## 9. Conceitos de domínio

- **Resultado de exame:** dado técnico do exame. Exame de origem, paciente, tipo do
  resultado, data, observação.
- **Resultado de imagem:** referência do arquivo, descrição, laudo técnico.
- **Resultado laboratorial:** conjunto de parâmetros medidos.
- **Item de resultado laboratorial:** nome do parâmetro, valor, unidade, faixa mínima e
  máxima de referência, resultado em texto, situação.

Exemplo de leitura esperada:

| Parâmetro | Resultado | Referência | Situação |
|---|---|---|---|
| Hemoglobina | 14,2 g/dL | 13,0 a 17,0 | Normal |
| Glicemia | 135 mg/dL | 70 a 99 | Acima da referência |
| HIV | Não reagente | Não reagente | Normal |

## 10. Eventos produzidos

| Evento | Quando ocorre | Quem se interessa |
|---|---|---|
| Resultado disponível | Resultado registrado | Paciente e médico solicitante |

## 11. Requisitos não funcionais

- Valor de exame, laudo e descrição **nunca** aparecem em log ou em payload de evento.
- Consulta de um resultado com até 50 parâmetros responde em menos de 500 ms.
- A referência de arquivo de imagem não expõe caminho interno do servidor.

## 12. Definition of Done

- [ ] RF-01 a RF-09 implementados e testados
- [ ] Teste unitário do cálculo de situação nas quatro situações possíveis
- [ ] Teste: registro em exame não realizado é rejeitado
- [ ] Teste: segundo resultado para o mesmo exame é rejeitado
- [ ] Teste: paciente não acessa resultado de terceiro
- [ ] Teste: parâmetro sem valor e sem texto é rejeitado
- [ ] Evento de resultado disponível publicado sem dado clínico
- [ ] Contrato OpenAPI publicado

## 13. Pendências

- [NEEDS CLARIFICATION: o resultado fica visível ao paciente imediatamente, ou só depois de haver parecer? Herdado da spec-mãe — decisão bloqueante para RF-07.]
- [NEEDS CLARIFICATION: retificação de resultado entra no MVP ou basta bloquear a alteração? (Assumido: bloquear alteração; retificação é evolução.)]
- [NEEDS CLARIFICATION: o arquivo de imagem é enviado à plataforma ou só referenciado por URL externa?]

## 14. Dependências

- Depende de: `001`, `002`, `004`
- Pré-requisito de: `006`
