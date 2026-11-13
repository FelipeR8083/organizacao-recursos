-- Dados exclusivamente ficticios para apresentacao academica.
-- A estrutura V1..V9 e criada pelo Flyway na inicializacao da aplicacao.
-- Protecao: executar SOMENTE no banco separado recursos_demo_v9.
-- Nao execute este arquivo no banco original "recursos".

BEGIN;

DO $$
BEGIN
    IF current_database() <> 'recursos_demo_v9' THEN
        RAISE EXCEPTION 'Seed bloqueado: banco atual (%) nao e recursos_demo_v9', current_database();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM flyway_schema_history WHERE version = '9' AND success = TRUE) THEN
        RAISE EXCEPTION 'Seed requer banco atualizado pelo Flyway ate V9';
    END IF;
END $$;

-- Recursos imaginarios, sem dados pessoais ou senhas.
INSERT INTO recurso (tipo, nome, em_manutencao, restrito, capacidade, localizacao, competencias)
VALUES
    ('SALA', 'Sala 201 - Demo V9', FALSE, FALSE, 30, 'Bloco A', 'Aula expositiva'),
    ('SALA', 'Laboratorio 202 - Demo V9', FALSE, TRUE, 20, 'Bloco B', 'Computadores'),
    ('PROFESSOR', 'Professor Ficticio - Demo V9', FALSE, FALSE, NULL, 'Bloco A', 'Programacao'),
    ('MATERIAL', 'Projetor A - Demo V9', FALSE, FALSE, NULL, 'Almoxarifado', 'Projetor multimidia'),
    ('MATERIAL', 'Notebook A - Demo V9', FALSE, FALSE, NULL, 'Almoxarifado', 'Notebook de apoio'),
    ('MATERIAL', 'Microfone B - Demo V9', TRUE, FALSE, NULL, 'Almoxarifado', 'Em manutencao')
ON CONFLICT DO NOTHING;

-- Reserva de exemplo encerrada, dois dias antes da carga (horario de Brasilia).
INSERT INTO reserva (recurso_id, professor_id, inicio, fim, estado, solicitante)
SELECT s.id, p.id,
       ((CURRENT_TIMESTAMP AT TIME ZONE 'America/Sao_Paulo')::date - 2 + TIME '09:00') AT TIME ZONE 'America/Sao_Paulo',
       ((CURRENT_TIMESTAMP AT TIME ZONE 'America/Sao_Paulo')::date - 2 + TIME '10:00') AT TIME ZONE 'America/Sao_Paulo',
       'CONCLUIDA', 'admin'
FROM recurso s CROSS JOIN recurso p
WHERE s.tipo = 'SALA' AND s.nome = 'Sala 201 - Demo V9'
  AND p.tipo = 'PROFESSOR' AND p.nome = 'Professor Ficticio - Demo V9'
  AND NOT EXISTS (
      SELECT 1 FROM reserva r
      WHERE r.recurso_id = s.id AND r.solicitante = 'admin'
        AND r.inicio = ((CURRENT_TIMESTAMP AT TIME ZONE 'America/Sao_Paulo')::date - 2 + TIME '09:00') AT TIME ZONE 'America/Sao_Paulo'
  );

-- Solicitacao futura para demonstrar aprovacao, autorizacao e disponibilidade.
INSERT INTO reserva (recurso_id, professor_id, inicio, fim, estado, solicitante)
SELECT s.id, p.id,
       ((CURRENT_TIMESTAMP AT TIME ZONE 'America/Sao_Paulo')::date + 1 + TIME '14:00') AT TIME ZONE 'America/Sao_Paulo',
       ((CURRENT_TIMESTAMP AT TIME ZONE 'America/Sao_Paulo')::date + 1 + TIME '15:00') AT TIME ZONE 'America/Sao_Paulo',
       'SOLICITADA', 'admin'
FROM recurso s CROSS JOIN recurso p
WHERE s.tipo = 'SALA' AND s.nome = 'Laboratorio 202 - Demo V9'
  AND p.tipo = 'PROFESSOR' AND p.nome = 'Professor Ficticio - Demo V9'
  AND NOT EXISTS (
      SELECT 1 FROM reserva r
      WHERE r.recurso_id = s.id AND r.solicitante = 'admin'
        AND r.inicio = ((CURRENT_TIMESTAMP AT TIME ZONE 'America/Sao_Paulo')::date + 1 + TIME '14:00') AT TIME ZONE 'America/Sao_Paulo'
  );

-- V7: reserva conjunta de sala, projetor e notebook.
INSERT INTO reserva_material (reserva_id, material_id)
SELECT r.id, m.id
FROM reserva r
JOIN recurso s ON s.id = r.recurso_id
JOIN recurso m ON m.tipo = 'MATERIAL' AND m.nome IN ('Projetor A - Demo V9', 'Notebook A - Demo V9')
WHERE s.nome = 'Sala 201 - Demo V9'
  AND r.solicitante = 'admin' AND r.estado = 'CONCLUIDA'
  AND r.inicio = ((CURRENT_TIMESTAMP AT TIME ZONE 'America/Sao_Paulo')::date - 2 + TIME '09:00') AT TIME ZONE 'America/Sao_Paulo'
ON CONFLICT DO NOTHING;

-- Reserva futura com um material; deixa o outro disponivel.
INSERT INTO reserva_material (reserva_id, material_id)
SELECT r.id, m.id
FROM reserva r
JOIN recurso s ON s.id = r.recurso_id
JOIN recurso m ON m.tipo = 'MATERIAL' AND m.nome = 'Projetor A - Demo V9'
WHERE s.nome = 'Laboratorio 202 - Demo V9'
  AND r.solicitante = 'admin' AND r.estado = 'SOLICITADA'
  AND r.inicio = ((CURRENT_TIMESTAMP AT TIME ZONE 'America/Sao_Paulo')::date + 1 + TIME '14:00') AT TIME ZONE 'America/Sao_Paulo'
ON CONFLICT DO NOTHING;

-- Historico identificando explicitamente que os registros foram importados.
INSERT INTO reserva_auditoria
    (reserva_id, estado_anterior, estado_novo, ocorrido_em, origem, tipo_evento, detalhes)
SELECT r.id, NULL, r.estado, CURRENT_TIMESTAMP, 'CARGA_DEMO', 'ESTADO',
       'Registro ficticio inserido pelo seed academico; nao e evidencia de execucao do fluxo real.'
FROM reserva r
JOIN recurso s ON s.id = r.recurso_id
WHERE s.nome IN ('Sala 201 - Demo V9', 'Laboratorio 202 - Demo V9')
  AND r.solicitante = 'admin'
  AND NOT EXISTS (
      SELECT 1 FROM reserva_auditoria ra
      WHERE ra.reserva_id = r.id AND ra.origem = 'CARGA_DEMO'
  );

-- Notificacao simulada, sem integracao SMTP nem envio real.
INSERT INTO notificacoes_simuladas
    (destinatario, assunto, mensagem, ocorrido_em, canal)
SELECT 'admin', 'DEMO V9 - notificacao ficticia',
       'Exemplo de notificacao simulada para a apresentacao academica.',
       CURRENT_TIMESTAMP, 'SIMULADO'
WHERE NOT EXISTS (
    SELECT 1 FROM notificacoes_simuladas
    WHERE assunto = 'DEMO V9 - notificacao ficticia'
);

COMMIT;
