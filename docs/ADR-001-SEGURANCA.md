# ADR-001 — Autenticação e autorização

- Status: adotada para o incremento acadêmico.
- Contexto: o sistema possui três perfis definidos no enunciado: Solicitante, Responsável e Administrador.
- Decisão: usar Spring Security com HTTP Basic sem sessão, usuários persistidos em PostgreSQL, senhas com BCrypt e autorização por papéis. O primeiro administrador é criado por variáveis de ambiente no primeiro start; o administrador cadastra os demais usuários.
- Regras: solicitantes e responsáveis consultam recursos/reservas; apenas administradores gerenciam recursos e usuários; responsáveis/administradores alteram estados e consultam relatórios/notificações; solicitantes só podem cancelar suas próprias reservas.
- Consequências: a interface usa autenticação do navegador; endpoints protegidos retornam 401 sem credenciais e 403 quando o papel não permite a ação. O canal HTTP Basic exige HTTPS fora de localhost. CSRF é desabilitado porque a API é stateless; se a autenticação migrar para cookie/sessão, a proteção CSRF deve ser reavaliada.
- Limitações: a alteração de reservas e a aprovação especial para recurso restrito ainda precisam de cenários completos de aceitação. Esta decisão não substitui revisão de segurança antes de produção.
