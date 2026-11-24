CREATE TABLE espacio (
    id_espacio BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE,
    tipo VARCHAR(20) NOT NULL UNIQUE,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO espacio (nombre, tipo, activo) VALUES
('Cancha', 'CANCHA', TRUE),
('Salón', 'SALON', TRUE);

CREATE TABLE reserva (
    id_reserva BIGINT AUTO_INCREMENT PRIMARY KEY,
    vecino_id BIGINT NOT NULL,
    espacio_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reserva_espacio FOREIGN KEY (espacio_id) REFERENCES espacio(id_espacio)
);

CREATE INDEX idx_reserva_disponibilidad
    ON reserva (espacio_id, fecha, hora_inicio, hora_fin, estado);

CREATE TABLE horario (
    id_horario BIGINT AUTO_INCREMENT PRIMARY KEY,
    dia_semana VARCHAR(15) NOT NULL UNIQUE,
    hora_apertura TIME,
    hora_cierre TIME,
    cerrado BOOLEAN NOT NULL DEFAULT FALSE
);
