# Foot Fanatics

API REST de conteúdo esportivo (clubes, jogos, resultados e matérias) com assinatura free e premium. Projeto-caso da disciplina de Qualidade de Software (TADS, Senac).

Autor: Guilherme Israel dos Santos

## Stack

Java 21 · Spring Boot 3.x · Maven · PostgreSQL

## Pré-requisitos

- Java 21 (`java -version`)
- Node.js (`node --version`), usado pelo AIOX
- Git

## Como subir o projeto

```bash
./mvnw spring-boot:run
```

## Como rodar os testes

```bash
./mvnw test
```

## CI e validação

Workflow GitHub Actions configurado em [.github/workflows/ci.yml](.github/workflows/ci.yml) com execução em `push` para `main` e em `pull_request`, usando Java 21 + Maven cache, `concurrency`, `timeout-minutes` e upload de relatórios Surefire/Failsafe com `if: always()`.

Run IDs registrados:
- Red: `35524224452` — falha intencional de asserção registrada para prova do ciclo vermelho
- Green: aguardando confirmação do run final após push do estado restaurado

Para proteger a `main` e exigir o check do workflow, o administrador do repositório pode usar:
1. GitHub → Settings → Branches → Add branch protection rule
2. Selecionar a branch `main`
3. Ativar `Require a pull request before merging`
4. Ativar `Require status checks to pass before merging`
5. Selecionar o check `verify` (ou o nome do job exibido no PR)
6. Salvar a regra

## Documentação

- [docs/prd.md](docs/prd.md): visão, personas, escopos e requisitos
- [docs/arquitetura.md](docs/arquitetura.md): stack, camadas, modelo de dados e decisões
- [docs/plano-de-teste.md](docs/plano-de-teste.md): estratégia, níveis, critérios e metas de cobertura
