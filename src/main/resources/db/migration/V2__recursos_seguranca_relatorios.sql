ALTER TABLE recurso ADD COLUMN IF NOT EXISTS capacidade INTEGER;
ALTER TABLE recurso ADD COLUMN IF NOT EXISTS localizacao VARCHAR(160);
ALTER TABLE recurso ADD COLUMN IF NOT EXISTS competencias VARCHAR(500);
CREATE INDEX IF NOT EXISTS idx_recurso_tipo_nome ON recurso(tipo, nome);
CREATE INDEX IF NOT EXISTS idx_recurso_localizacao ON recurso(localizacao);
CREATE UNIQUE INDEX IF NOT EXISTS uk_recurso_tipo_nome_normalizado
    ON recurso(tipo, lower(trim(nome)));

ALTER TABLE reserva ADD COLUMN IF NOT EXISTS professor_id BIGINT;
ALTER TABLE reserva ADD CONSTRAINT fk_reserva_professor
    FOREIGN KEY (professor_id) REFERENCES recurso(id);

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(80) NOT NULL,
    senha_hash VARCHAR(100) NOT NULL,
    perfil VARCHAR(20) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_usuario_login UNIQUE (login)
);

CREATE TABLE IF NOT EXISTS conflitos_evitados (
    id BIGSERIAL PRIMARY KEY,
    recurso_id BIGINT NOT NULL,
    ocorrido_em TIMESTAMPTZ NOT NULL,
    inicio_solicitado TIMESTAMPTZ NOT NULL,
    fim_solicitado TIMESTAMPTZ NOT NULL,
    motivo VARCHAR(120) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_conflito_ocorrido_em ON conflitos_evitados(ocorrido_em);

CREATE TABLE IF NOT EXISTS notificacoes_simuladas (
    id BIGSERIAL PRIMARY KEY,
    destinatario VARCHAR(80) NOT NULL,
    assunto VARCHAR(100) NOT NULL,
    mensagem VARCHAR(500) NOT NULL,
    ocorrido_em TIMESTAMPTZ NOT NULL,
    canal VARCHAR(20) NOT NULL DEFAULT 'SIMULADO'
);
CREATE INDEX IF NOT EXISTS idx_notificacao_destinatario_data
    ON notificacoes_simuladas(destinatario, ocorrido_em);
