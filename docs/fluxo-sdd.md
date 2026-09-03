# Como trabalhamos: Spec-Driven Development

Guia prático da equipe. A regra formal está na
[constituição](../.specify/memory/constitution.md); aqui está o dia a dia.

## A ideia

Em vez de escrever código e depois documentar, escrevemos a intenção primeiro e derivamos
o código dela. A spec é o artefato que dura; o código é a implementação atual dessa spec.

Três documentos por feature, cada um respondendo uma pergunta diferente:

| Documento | Pergunta | Linguagem | Quem revisa |
|---|---|---|---|
| `spec.md` | O que e por quê | Negócio | Todo mundo |
| `plan.md` | Como | Técnica | Luis + dono da feature |
| `tasks.md` | Em que ordem, por quem | Execução | Dono da feature |

## O ciclo

```
1. /specify   escreve a spec de negócio        → revisão da equipe
2. resolver   eliminar os [NEEDS CLARIFICATION] → spec vira "Aprovada"
3. /plan      escreve o plano técnico           → revisão de arquitetura
4. /tasks     quebra em tarefas                 → distribui
5. /implement executa fase por fase             → build verde
6. /analyze   audita spec × plano × código      → corrige divergência
```

### Passo 2 é o mais importante

`[NEEDS CLARIFICATION]` não é enfeite. É onde a spec admite que alguém precisa decidir.
Ignorar um marcador significa que a decisão vai ser tomada implicitamente por quem
programar — no pior momento e sem registro.

Como resolver: leve a pergunta para a equipe, decida, **apague o marcador e escreva a
regra na spec**. Se a decisão tem custo, registre o porquê.

## O que vai em cada documento

**Na `spec.md` pode:** ator, permissão, regra de negócio, critério de aceite, fluxo de
exceção, evento de negócio.
**Na `spec.md` não pode:** Java, Spring, tabela, coluna, endpoint, nome de classe.

Teste rápido: se a frase deixaria de fazer sentido caso trocássemos Spring por outro
framework, ela não pertence à spec.

**No `plan.md`:** estrutura de pacotes, casos de uso, contrato REST, DDL, eventos,
estratégia de teste, e as exceções à constituição em `Complexity Tracking`.

## Quando a implementação discorda da spec

Acontece — e é sinal de que a spec aprendeu algo. O procedimento é:

1. Pare a implementação.
2. Atualize a `spec.md` com a regra correta.
3. Ajuste `plan.md` e `tasks.md` se necessário.
4. Retome.

O que **não** vale é ajustar só o código: na próxima leitura, ninguém saberá qual dos dois
está certo.

## Ordem de implementação dentro de uma feature

Sempre de dentro para fora:

```
contrato + migration → domínio → casos de uso → infraestrutura → controller → integração
```

O domínio é escrito e testado sem Spring. Se para testar uma regra de negócio você precisou
subir o contexto do Spring, a regra está no lugar errado.

## Definição de pronto

Uma feature está pronta quando:

- todos os RF da spec têm teste passando;
- existe teste de autorização negativa (role errada e paciente lendo dado de terceiro);
- `./mvnw verify` está verde, incluindo cobertura e verificação de módulos;
- o Swagger mostra o recurso conforme o contrato;
- a spec descreve o que o código realmente faz.

## Convenção de numeração

`NNN-slug`, sequencial na ordem em que a feature foi especificada. O número nunca é
reaproveitado, mesmo se a feature for descartada. Faixas de migration por módulo estão em
[`plan.md` da spec-mãe](../specs/000-plataforma-sus/plan.md#8-persistência-e-migrations).

## Perguntas frequentes

**"É burocracia demais para um hackathon?"**
O custo está em escrever a spec, e ela substitui a reunião de alinhamento que aconteceria
de qualquer jeito. Com cinco pessoas trabalhando em módulos que se referenciam, a
alternativa a especificar é retrabalho de integração no último dia.

**"Posso pular o `plan.md` numa feature pequena?"**
Não, mas ele pode ser curto. O portão constitucional e o contrato REST são obrigatórios;
o resto é proporcional ao tamanho.

**"E se eu já sei exatamente o que vou codar?"**
Então escrever a spec custa dez minutos. Se custar mais, você não sabia.
