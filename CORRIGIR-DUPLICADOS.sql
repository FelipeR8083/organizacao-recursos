-- Execute UMA VEZ no banco recursos, com o aplicativo parado.
-- Faz backup antes. Preserva IDs e vínculos de reservas.
BEGIN;
DO $$
DECLARE r RECORD; candidato TEXT; sufixo INT;
BEGIN
  FOR r IN
    SELECT id, tipo, nome FROM (
      SELECT id, tipo, nome, row_number() OVER (PARTITION BY tipo, lower(btrim(nome)) ORDER BY id) AS ordem
      FROM recurso
    ) x WHERE ordem > 1 ORDER BY id
  LOOP
    candidato := btrim(r.nome) || ' - ID ' || r.id;
    sufixo := 2;
    WHILE EXISTS (
      SELECT 1 FROM recurso WHERE tipo = r.tipo AND lower(btrim(nome)) = lower(btrim(candidato)) AND id <> r.id
    ) LOOP
      candidato := btrim(r.nome) || ' - ID ' || r.id || ' (' || sufixo || ')';
      sufixo := sufixo + 1;
    END LOOP;
    UPDATE recurso SET nome = candidato WHERE id = r.id;
  END LOOP;
END $$;
CREATE UNIQUE INDEX IF NOT EXISTS ux_recurso_tipo_nome_normalizado ON recurso (tipo, lower(btrim(nome)));
COMMIT;
-- Conferir: SELECT id, tipo, nome FROM recurso ORDER BY id;
