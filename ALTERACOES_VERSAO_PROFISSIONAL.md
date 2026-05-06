# Alterações da versão profissional

## O que foi removido
- Removido `StateController.java`.
- Removidas as chamadas do front para `/api/state`.

## Como o front conecta agora
O arquivo `static/js/script.js` agora carrega e salva dados usando endpoints reais:

- `/users`
- `/trips`
- `/payments`
- `/seats`
- `/rooms`

O `localStorage` ficou apenas para dados temporários de tela:

- sessão atual (`iv_session`)
- viagem ativa (`iv_active_trip`)

## Endpoints adicionados/ajustados
Foram adicionados métodos de listagem completa e salvamento em lote para manter o front sincronizado com o banco:

- `PUT /users/bulk`
- `PUT /trips/bulk`
- `PUT /payments/bulk`
- `PUT /seats/bulk`
- `PUT /rooms/bulk`

Também foram adicionados alguns endpoints auxiliares como busca por ID/CPF e delete.

## Observação sobre o banco
Como o projeto já tinha tabelas antigas criadas, pode aparecer erro de alteração de coluna/chave estrangeira se o Hibernate tentar modificar estruturas existentes.

Para teste/desenvolvimento, a solução mais limpa é apagar o banco `viagensIgreja_DB` e criar novamente, ou usar outro schema vazio.
