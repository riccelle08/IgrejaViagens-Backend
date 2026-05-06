# Alterações feitas

## Objetivo
Refatorar a camada de dados do front-end para parar de usar `localStorage` como banco de dados, mantendo o mesmo design das telas.

## Front-end
Arquivo alterado:

- `src/main/resources/static/js/script.js`

Principais mudanças:

- O `DB` deixou de gravar dados do sistema no navegador.
- O front agora carrega os dados de `/api/state` ao iniciar a página.
- Alterações em usuários, viagens, pagamentos, quartos e assentos são enviadas para o backend e persistidas no banco.
- Apenas `iv_session` e `iv_active_trip` continuam no navegador, porque são dados temporários de navegação da tela.
- Corrigido o formulário de login, que estava com listener duplicado.

## Backend
Arquivos criados:

- `src/main/java/br/com/viagensigreja/controller/StateController.java`
- `src/main/java/br/com/viagensigreja/controller/BusController.java`

Arquivos ajustados:

- Models: `Trip`, `Bus`, `Payment`, `Room`, `Seat`
- Services: `TripService`, `BusService`, `PaymentService`, `RoomService`, `SeatService`
- Repositories/Controllers para aceitar IDs compatíveis com o front.

## Nova rota principal

```http
GET /api/state
POST /api/state
```

Essa rota sincroniza o estado usado pelas telas com as tabelas reais do Spring/JPA.

## Observação
Não alterei o HTML nem o CSS principal. O design foi mantido.
