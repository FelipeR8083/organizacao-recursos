CREATE TABLE recurso (
    id BIGSERIAL PRIMARY KEY,
    tipo VARCHAR(30) NOT NULL,
    nome VARCHAR(255) NOT NULL,
    em_manutencao BOOLEAN NOT NULL DEFAULT FALSE,
    restrito BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE reserva (
    id BIGSERIAL PRIMARY KEY,
    recurso_id BIGINT NOT NULL REFERENCES recurso(id),
    inicio TIMESTAMPTZ NOT NULL,
    fim TIMESTAMPTZ NOT NULL,
    estado VARCHAR(30) NOT NULL,
    solicitante VARCHAR(255) NOT NULL,
    CONSTRAINT ck_reserva_periodo CHECK (fim > inicio)
);

CREATE INDEX idx_reserva_recurso_periodo ON reserva(recurso_id, inicio, fim);
CREATE INDEX idx_reserva_estado ON reserva(estado);

CREATE TABLE reserva_auditoria (
    id BIGSERIAL PRIMARY KEY,
    reserva_id BIGINT NOT NULL REFERENCES reserva(id),
    estado_anterior VARCHAR(30),
    estado_novo VARCHAR(30) NOT NULL,
    ocorrido_em TIMESTAMPTZ NOT NULL,
    origem VARCHAR(80) NOT NULL
);

CREATE INDEX idx_auditoria_reserva_data_id ON reserva_auditoria(reserva_id, ocorrido_em, id);
