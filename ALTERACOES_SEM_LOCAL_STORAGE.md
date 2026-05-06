# Alterações — remoção do Local Storage

- Removido todo uso de `localStorage` do `static/js/script.js`.
- Dados principais agora são carregados e salvos apenas pelos endpoints reais:
  - `/users`
  - `/trips`
  - `/payments`
  - `/seats`
  - `/rooms`
- A sessão do usuário e a viagem ativa foram movidas para cookies simples, apenas para manter navegação entre páginas.
- O banco continua sendo a fonte oficial dos dados.

## Atenção
Se o navegador ainda mostrar chaves antigas como `iv_users`, `iv_trips`, `iv_payments`, `iv_rooms` ou `iv_seats` no DevTools, elas são sobras da versão antiga. Limpe manualmente uma vez em:

DevTools > Application > Local Storage > botão direito no site > Clear.

Depois disso, ao cadastrar/editar dados, essas chaves não serão recriadas pelo novo `script.js`.
