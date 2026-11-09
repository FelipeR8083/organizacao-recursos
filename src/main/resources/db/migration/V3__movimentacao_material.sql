CREATE TABLE IF NOT EXISTS movimentacoes_material (
    id BIGSERIAL PRIMARY KEY,
    reserva_id BIGINT NOT NULL UNIQUE REFERENCES reserva(id),
    recurso_id BIGINT NOT NULL REFERENCES recurso(id),
    retirada_em TIMESTAMPTZ NOT NULL,
    retirada_por VARCHAR(80) NOT NULL,
    devolucao_em TIMESTAMPTZ,
    devolucao_por VARCHAR(80),
    CONSTRAINT ck_movimentacao_devolucao
        CHECK ((devolucao_em IS NULL AND devolucao_por IS NULL)
            OR (devolucao_em IS NOT NULL AND devolucao_por IS NOT NULL))
);
