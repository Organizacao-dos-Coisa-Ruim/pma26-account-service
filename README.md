# pma26-account-service

Implementação do serviço **Account** (cadastro de usuários). Implementa a interface
[`pma-26-account`](https://github.com/Organizacao-dos-Coisa-Ruim/pma-26-account), onde estão as rotas e os DTOs.

## Estrutura (`pma.account`)

| Classe | Papel |
|--------|-------|
| `AccountResource` | Controller: implementa `AccountController` e monta as respostas HTTP |
| `AccountService` | Regras: validação, email único, hash de senha, login |
| `AccountRepository` | Spring Data JPA |
| `AccountModel` | Entidade da tabela `account.account` |
| `AccountParser` | Model → `AccountOut` (a senha nunca sai) |
| `PasswordConfig` | Bean do `BCryptPasswordEncoder` |

Senhas são guardadas só como hash **BCrypt** (salt aleatório embutido no hash).
O schema do banco é criado pelo **Flyway** (`src/main/resources/db/migration`).

## Variáveis de ambiente

| Variável | Padrão |
|----------|--------|
| `DATABASE_HOST` | `localhost` |
| `DATABASE_PORT` | `5432` |
| `DATABASE_DB` | `store` |
| `DATABASE_USERNAME` | `store` |
| `DATABASE_PASSWORD` | – (obrigatória) |

## Build, testes e imagem

```bash
# 1. instalar a interface (uma vez, ou quando ela mudar)
cd ../pma-26-account && ./mvnw clean install && cd -

# 2. testes + jar (os testes usam H2 em memória, não precisam de Postgres nem Docker)
./mvnw clean package

# 3. imagem
docker build -t account .
docker run -p 8080:8080 -e DATABASE_HOST=host.docker.internal -e DATABASE_PASSWORD=... account
```

## Endpoints

Ver o README da interface. Resumo: `POST /accounts`, `GET /accounts`, `GET /accounts/{id}`,
`DELETE /accounts/{id}`, `POST /accounts/login`, `GET /accounts/health-check`.
