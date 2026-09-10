# Igreja Viagens

Aplicacao Spring Boot com PostgreSQL.

## Executar com Docker

Suba a aplicacao e o banco de dados:

```bash
docker compose up --build
```

A aplicacao ficara disponivel em <http://localhost:8080>. Os dados do PostgreSQL
sao mantidos no volume Docker `postgres_data`, inclusive quando os containers
sao parados ou recriados.

Para parar os containers:

```bash
docker compose down
```

Para apagar tambem os dados persistidos do banco:

```bash
docker compose down -v
```

As portas e credenciais podem ser alteradas em um arquivo `.env`:

```dotenv
APP_PORT=8080
DB_PORT=5432
DB_NAME=igreja_viagens
DB_USERNAME=igreja
DB_PASSWORD=igreja
```

## Seguranca: pendencias da autenticacao por sessao

A API exige uma sessao autenticada, mas ainda existem duas protecoes pendentes:

- autorizacao detalhada por perfil: nesta fase, `ADMIN` e `TRAVELER` autenticados
  ainda possuem o mesmo acesso aos endpoints;
- CSRF: a protecao esta temporariamente desabilitada porque o React atual nao
  envia o token nas operacoes de escrita.

O fluxo atual pressupoe frontend e backend na mesma origem (incluindo o proxy do
Vite). Em producao HTTPS, configure `SESSION_COOKIE_SECURE=true`. Uma implantacao
com origens diferentes tambem exigira CORS com credenciais e `credentials` no
cliente React.
