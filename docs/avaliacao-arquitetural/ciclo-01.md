# Ciclo 01 — Avaliação arquitetural ATAM

## 1. Objetivo do ciclo

Este primeiro ciclo de ATAM foi conduzido como avaliação de arquitetura orientada por evidência, valendo-se das fontes documentais do projeto e dos testes executáveis disponíveis. A intenção foi verificar se a arquitetura declarada suporta os drivers de negócio e qualidade expressos em [../prd.md](../prd.md), [../arquitetura.md](../arquitetura.md) e no conjunto de testes de regra de negócio em [../../src/test/java/br/senac/footfanatics/BusinessRulesRedPhaseTest.java](../../src/test/java/br/senac/footfanatics/BusinessRulesRedPhaseTest.java).

A análise foi feita com rigor de não invenção: tudo o que for afirmado a seguir precisa ter apoio direto no PRD, na arquitetura, ou na evidência de código/teste. Onde faltam requisitos ou configuração explícita, a conclusão foi registrada como lacuna ou risco, não como premissa.

## 2. Fontes de evidência adotadas

- [../prd.md](../prd.md): contexto, personas, escopos, requisitos funcionais e não funcionais.
- [../arquitetura.md](../arquitetura.md): stakeholders, drivers, riscos, restrições, visão de solução, fronteiras e diagramas.
- [../taticas-arquiteturais-len-bass.md](../taticas-arquiteturais-len-bass.md): catalogo de táticas arquiteturais de Len Bass, Clements e Kazman usado como referência para nomear as táticas.
- [../../src/test/java/br/senac/footfanatics/BusinessRulesRedPhaseTest.java](../../src/test/java/br/senac/footfanatics/BusinessRulesRedPhaseTest.java): suíte executável de regras de negócio e atributos de qualidade.
- [../../src/main/resources/application.properties](../../src/main/resources/application.properties): configuração de runtime da aplicação.
- [../../src/test/java/br/senac/footfanatics/TestcontainersConfiguration.java](../../src/test/java/br/senac/footfanatics/TestcontainersConfiguration.java): evidência de banco em ambiente de teste.

## 3. Contexto do sistema e do negócio

O projeto descreve uma API de conteúdo esportivo com três personas principais:

- Rafael: torcedor casual, plano free, acessa conteúdo público sem login.
- Marina: torcedora premium, exige sessão, vigência, continuidade e acesso sem reautenticação indevida.
- Carlos: editor de conteúdo, publica, marca conteúdo e precisa de autorização e rastreabilidade.

O sistema possui dois domínios arquiteturalmente relevantes já declarados:

1. Fluxo online: autenticação, sessão, assinatura, conteúdo, autorização e publicação editorial.
2. Fluxo batch: cron diário final do dia, processando somente dados novos.

A restrição operacional obrigatória no documento de arquitetura também foi tratada como estímulo arquitetural: o cron diário trabalha apenas com dados novos e não deve reutilizar dados antigos fora do conjunto novo.

## 4. Drivers de negócio e qualidade priorizados

### 4.1 Drivers priorizados

| Driver | Descrição | Evidência principal |
|---|---|---|
| D1 | Acesso seguro e consistente para usuários autenticados | [../prd.md](../prd.md), RF-05 a RF-08, RNF-01 a RNF-04 |
| D2 | Conteúdo público x premium sem vazamento ou bloqueio indevido | [../prd.md](../prd.md), RF-13 a RF-15, RNF-06, RNF-07, RNF-09 |
| D3 | Gestão de assinatura com vigência e cancelamento dentro da regra explícita | [../prd.md](../prd.md), RF-09 a RF-12, RNF-05 |
| D4 | Publicação editorial com autorização e rastreabilidade | [../prd.md](../prd.md), RF-16, RF-17, RNF-08, RNF-12 |
| D5 | Batch incremental diário apenas com dados novos | [../arquitetura.md](../arquitetura.md), restrição operacional obrigatória |

### 4.2 Atributos de qualidade explícitos no projeto

Os atributos mais evidentes são:

- Segurança
- Confiabilidade da sessão
- Integridade do conteúdo
- Desempenho percebido
- Disponibilidade
- Rastreabilidade

Esses atributos foram consolidados em [../arquitetura.md](../arquitetura.md), seção 3.

## 5. Utility tree e cenários de qualidade

A utility tree abaixo foi montada com base no PRD, na arquitetura e na suíte de testes. A prioridade foi dada aos cenários que afetam diretamente o valor percebido do produto e a segurança do sistema.

### 5.1 Utility tree (prioridades)

| Prioridade | Atributo | Cenário-chave | Tática principal esperada |
|---|---|---|---|
| P1 | Segurança | Token ausente, inválido ou expirado deve resultar em 401 e não ser gravado em log | Tática de autenticação/validação + tratamento de exceção |
| P2 | Integridade do conteúdo | Conteúdo exclusivo não deve ser acessado por visitante ou free; premium ativo deve receber o conteúdo completo | Tática de autorização e contenção de acesso |
| P3 | Segurança/Confiabilidade | Senhas devem ser armazenadas em hash; credenciais não podem ficar em texto puro | Tática de proteção de dados e prevenção de vazamento |
| P4 | Sessão | Usuários premium devem manter sessão consistente com expiração correta e apoio a múltiplos dispositivos | Tática de gerenciamento de sessão |
| P5 | Desempenho | Leitura pública e bloqueio de conteúdo devem responder em p95 conforme os limites | Tática de desempenho e degradação |
| P6 | Rastreabilidade | Publicações e mudanças de visibilidade devem registrar usuário e timestamp | Tática de auditoria e monitoramento |
| P7 | Disponibilidade | Sistema deve manter disponibilidade compatível com health check periódico | Monitoramento e detecção de falhas |
| P8 | Modificabilidade | Batch incremental deve processar apenas dados novos sem reuso de dados antigos | Defer binding / filtros em batch |

### 5.2 Cenários de atributo de qualidade

| ID | Fonte | Estímulo | Ambiente | Artefato | Resposta esperada | Métrica | Prioridade |
|---|---|---|---|---|---|---|---|
| ATAM-01 | Cliente sem autenticação | Requisição com token ausente/inválido | Operação normal | camada de autenticação/API | Rejeitar e responder 401 | 100% dos casos retornam 401 | P1 |
| ATAM-02 | Usuário free/visitante | Tentativa de acesso a conteúdo exclusivo | Operação normal | serviço de conteúdo | Bloquear sem devolver o corpo da matéria | p95 < 1 s | P2 |
| ATAM-03 | Usuário premium | Acesso a matéria exclusiva ativa | Operação normal | serviço de conteúdo | Liberar conteúdo completo | até 30 s após contratação; sem vazamento | P2 |
| ATAM-04 | Ataque ou falha de autenticação | Senha em texto puro ou hashing ausente | Persistência e runtime | armazenamento de credenciais | Salvar hash e nunca texto puro | 100% dos casos | P3 |
| ATAM-05 | Usuário autenticado | Sessão expirada ou inválida | Operação normal | serviço de sessão | Forçar nova autenticação | 60 min sem requisições; 0 expiração indevida em 2 h | P4 |
| ATAM-06 | Leitura pública | 100 requisições simultâneas | Rede móvel e uso real | endpoint de resultados | Responder rapidamente | p95 < 2 s | P5 |
| ATAM-07 | Editor | Publicação ou alteração de visibilidade | Operação normal | API editorial | Registrar usuário e data/hora | 100% dos eventos auditáveis | P6 |
| ATAM-08 | Job diário | Processamento de dados novos | Execução de batch | Cron diário | Processar somente dados novos | watermark/checkpoint preservado | P8 |

## 6. Mapeamento de táticas arquiteturais para os cenários

Com base no catálogo de Len Bass e no projeto, as táticas mais compatíveis com os cenários são:

| Cenário | Tática arquitetural | Justificativa |
|---|---|---|
| ATAM-01 | Autenticação com validação de token e tratamento de exceção | RF-07 e RNF-04 exigem rejeição explícita de token inválido/expirado |
| ATAM-02 | Autorização por domínio + bloqueio explícito | RF-14 e RNF-07 definem conteúdo exclusivo como área de acesso controlado |
| ATAM-03 | Regras de negócio por perfil + vigência de assinatura | RF-15, RF-11, RNF-05 determinam a liberação premium ativa |
| ATAM-04 | Prevention/exception prevention para armazenamento seguro | RNF-01 exige hash e proíbe texto puro |
| ATAM-05 | Session management + timeout/expiração | RF-05 a RF-08 e RNF-03 detalham as regras de sessão |
| ATAM-06 | Performance: limit event response + increase resource efficiency | RNF-06 e RNF-10 fixam limites de tempo e payload |
| ATAM-07 | Audit trail + monitoring | RNF-12 exige rastreabilidade de publicação e mudança de marcação |
| ATAM-08 | Defer binding / checkpoint incremental | Restrição operacional do cron exige processamento filtrado por dados novos |

Observação importantíssima: o documento de arquitetura menciona “tática” apenas de forma conceitual; este ciclo ATAM faz a tradução dessas decisões em termos funcionais e de atributo de qualidade. Sem esse nomeamento, a arquitetura ficaria menos rastreável para análise e trade-off.

## 7. Evidência de implementação e testes

A análise de evidência de arquitetura foi reforçada pela suíte de testes e pela estrutura do projeto atual.

### 7.1 Evidência documental

- [../prd.md](../prd.md): traz as regras específicas de cadastro, sessão, assinatura, conteúdo e autorização.
- [../arquitetura.md](../arquitetura.md): organiza drivers, riscos, visão de alto nível e limites da solução.

### 7.2 Evidência executável

A suíte [../../src/test/java/br/senac/footfanatics/BusinessRulesRedPhaseTest.java](../../src/test/java/br/senac/footfanatics/BusinessRulesRedPhaseTest.java) valida:

- cadastro e restrição por e-mail duplicado/ inválido;
- autenticação e expiração de sessão;
- assinatura free/premium e vigência;
- acesso público x premium;
- perfil editor x torcedor;
- UX de bloqueio e auditoria;
- regra de batch incremental.

Esses cenários confirmam que o projeto está no caminho correto em relação às metas declaradas, mas ainda não há prova de implementação de produção dos módulos, apenas de regra de negócio em testes isolados.

## 8. Trade-offs e pontos de sensibilidade

### 8.1 Trade-off principal

O principal trade-off do projeto está entre:

- segurança/autorização e
- desempenho percebido.

Exemplo: a regra de bloquear conteúdo exclusivo de forma rápida (RNF-07) exige validação imediata antes da entrega do corpo da matéria. Isso melhora segurança e integridade, mas pode aumentar a complexidade do serviço de autorização e exigir checagens rápidas em cada leitura.

### 8.2 Sensibilidade em sessão e assinatura

O documento de arquitetura registra ambiguidades em RF-08 e RF-12. Isso gera sensibilidade em:

- política de múltiplos dispositivos simultâneos;
- definição exata de cancelamento e vigência;
- sincronização entre status premium, conteúdo e acesso.

Esses pontos não estão resolvidos como decisão arquitetural formal, mas são claramente relevantes para a robustez do sistema.

### 8.3 Sensibilidade em batch e dados incrementais

A restrição do cron diário exige que o batch use somente dados novos. Isso cria trade-off entre:

- integridade e recuperação do modelo;
- custo de processamento incremental;
- rastreabilidade do checkpoint/watermark.

O conjunto de testes menciona explicitamente watermark/checkpoint, o que confirma que a intenção arquitetural está presente, mas não implementada como componente de produção.

## 9. Riscos e lacunas arquiteturais

### 9.1 Riscos confirmados

| Risco | Status | Observação |
|---|---|---|
| Política de múltiplos dispositivos simultâneos não definida | Aberto | RF-08 não define limite ou mecanismo |
| Cancelamento e vigência ainda ambíguos | Aberto | RF-12 exige confirmação de “acesso até o fim do período contratado” |
| Sem modelagem de dados formal | Aberto | Falta entidade, schema e persistência explícita |
| Sem banco de produção configurado | Confirmado | Aplicação não define datasource no [../../src/main/resources/application.properties](../../src/main/resources/application.properties) |
| Sem evidência de batch em produção | Aberto | Há requisito e teste, mas não implementação de cron/serviço real |

### 9.2 Conclusão de risco

A arquitetura está coerente no nível de requisitos e cenários, mas ainda não está “fechada” em termos de estrutura de dados, persistência, serviços e integração. O projeto está em uma fase de concepção de domínio e regras explícitas, não em uma fase de implementação operacional completa.

## 10. Estado da persistência e do banco de dados

Este ponto foi verificado diretamente:

- [../../src/main/resources/application.properties](../../src/main/resources/application.properties) contém apenas:
  - spring.application.name=foot-fanatics
- Não há datasource, URL de banco, usuário, senha ou driver de produção configurados.
- O único registro de banco no projeto é o ambiente de teste em [../../src/test/java/br/senac/footfanatics/TestcontainersConfiguration.java](../../src/test/java/br/senac/footfanatics/TestcontainersConfiguration.java), que inicia um PostgreSQL via Testcontainers.

Conclusão factual: o projeto, neste estado, não possui banco de dados configurado para produção. O banco existente é de teste, provisionado automaticamente via Testcontainers, e não substitui a persistência de runtime do sistema em produção.

## 11. Avaliação do ciclo ATAM

### 11.1 Resultado geral

O primeiro ciclo ATAM concluiu que a arquitetura está alinhada aos drivers mais críticos do problema, especialmente em:

- autenticação e sessão;
- distinção de conteúdo público x premium;
- autorização editorial;
- rastreabilidade básica de publicações;
- batch incremental com checkpoint.

### 11.2 No que a arquitetura ainda está fraca

Os pontos de maior fragilidade são:

- ausência de modelo de dados explícito;
- ausência de configuração real de banco de produção;
- falta de decisão formal sobre múltiplos dispositivos simultâneos;
- falta de decisão formal sobre duração exata de cancelamento e estado da assinatura;
- ausência de definição operacional do batch de treinamento.

### 11.3 Decisão do ciclo

O ciclo 01 deve ser considerado um ciclo de avaliação inicial de alinhamento arquitetural, com boa aderência aos requisitos do PRD e aos testes de regra de negócio, mas com lacunas significativas ainda pendentes antes de considerar a solução “pronta para implementação de produção”.

## 12. Próximos passos recomendados

1. Definir arquitetura de persistência e banco de dados de produção.
2. Formalizar políticas de sessão e múltiplos dispositivos simultâneos.
3. Definir regra exata de cancelamento e vigência premium.
4. Especificar watermark/checkpoint do batch incremental.
5. Dar continuidade ao ciclo 02 com cenários de risco, avaliação de arquitetura gerada e decisão de implementação.

## 13. Resumo executivo

A análise ATAM do ciclo 01 indica que o projeto tem um conjunto de requisitos e drivers muito claros, com foco em segurança, conteúdo premium e autenticação. A arquitetura declarada é consistente com esses objetivos e com o conjunto de testes. No entanto, ela ainda não está concluída em termos de persistência real, banco de produção e decisões de operação. A maior conclusão objetiva do ciclo é esta: a solução ainda não possui um banco de dados de produção configurado; o que existe hoje é um banco de teste em container via Testcontainers.
