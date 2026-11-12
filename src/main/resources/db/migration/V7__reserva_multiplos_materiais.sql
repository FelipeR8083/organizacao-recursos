CREATE TABLE reserva_material (
    reserva_id BIGINT NOT NULL REFERENCES reserva(id) ON DELETE CASCADE,
    material_id BIGINT NOT NULL REFERENCES recurso(id),
    PRIMARY KEY (reserva_id, material_id)
);
CREATE INDEX idx_reserva_material_material ON reserva_material(material_id);
