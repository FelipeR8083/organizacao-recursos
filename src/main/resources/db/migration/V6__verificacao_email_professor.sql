-- Estado de verificacao separado da aprovacao administrativa.
-- Contas existentes permanecem com verificacao nao exigida; novos cadastros
-- publicos terao a verificacao obrigatoria ao concluir a implementacao.
ALTER TABLE usuarios ADD COLUMN verificacao_email_obrigatoria BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE usuarios ADD COLUMN email_verificado_em TIMESTAMPTZ;

CREATE TABLE verificacao_email_professor (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    token_hash CHAR(64) NOT NULL UNIQUE,
    expira_em TIMESTAMPTZ NOT NULL,
    consumido_em TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_verificacao_email_usuario ON verificacao_email_professor(usuario_id);
CREATE INDEX idx_verificacao_email_expiracao ON verificacao_email_professor(expira_em);
