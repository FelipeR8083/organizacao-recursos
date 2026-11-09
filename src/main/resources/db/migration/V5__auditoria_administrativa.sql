CREATE TABLE auditoria_administrativa (
    id BIGSERIAL PRIMARY KEY,
    entidade VARCHAR(30) NOT NULL,
    referencia_id BIGINT NOT NULL,
    acao VARCHAR(30) NOT NULL,
    ator VARCHAR(80) NOT NULL,
    detalhes VARCHAR(500),
    ocorrido_em TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_auditoria_admin_data ON auditoria_administrativa(ocorrido_em);
