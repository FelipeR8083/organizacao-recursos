-- V3 allowed only one material movement per reservation.
-- Keep existing records and allow one movement for each material associated with a room reservation.
ALTER TABLE movimentacoes_material
    DROP CONSTRAINT IF EXISTS movimentacoes_material_reserva_id_key;

ALTER TABLE movimentacoes_material
    ADD CONSTRAINT uk_movimentacao_reserva_recurso
        UNIQUE (reserva_id, recurso_id);
