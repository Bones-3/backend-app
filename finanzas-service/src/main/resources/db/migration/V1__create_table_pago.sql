CREATE TABLE cuota (
    id_cuota BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(500),
    tipo VARCHAR(20) NOT NULL,
    monto DECIMAL(14,2) NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cuota_vecino (
    id_cuota_vecino BIGINT AUTO_INCREMENT PRIMARY KEY,
    cuota_id BIGINT NOT NULL,
    vecino_id BIGINT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    monto_pagado DECIMAL(14,2) NOT NULL DEFAULT 0,
    fecha_asignacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cuota_vecino_cuota FOREIGN KEY (cuota_id) REFERENCES cuota(id_cuota),
    CONSTRAINT uk_cuota_vecino UNIQUE (cuota_id, vecino_id)
);

CREATE TABLE pago (
    id_pago BIGINT AUTO_INCREMENT PRIMARY KEY,
    cuota_vecino_id BIGINT NOT NULL,
    vecino_id BIGINT NOT NULL,
    total DECIMAL(14,2) NOT NULL,
    metodo_pago VARCHAR(50) NOT NULL,
    fecha_pago DATETIME NOT NULL,
    CONSTRAINT fk_pago_cuota_vecino FOREIGN KEY (cuota_vecino_id) REFERENCES cuota_vecino(id_cuota_vecino)
);

CREATE TABLE comprobante (
    id_comprobante BIGINT AUTO_INCREMENT PRIMARY KEY,
    pago_id BIGINT NOT NULL UNIQUE,
    nombre_archivo VARCHAR(255) NOT NULL,
    url VARCHAR(1000) NOT NULL,
    mime_type VARCHAR(100),
    fecha_carga DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comprobante_pago FOREIGN KEY (pago_id) REFERENCES pago(id_pago)
);

CREATE TABLE movimiento (
    id_movimiento BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL,
    categoria VARCHAR(80) NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    monto DECIMAL(14,2) NOT NULL,
    fecha DATE NOT NULL,
    pago_id BIGINT,
    CONSTRAINT fk_movimiento_pago FOREIGN KEY (pago_id) REFERENCES pago(id_pago)
);

CREATE INDEX idx_cuota_vencimiento ON cuota (fecha_vencimiento, activa);
CREATE INDEX idx_cuota_vecino_estado ON cuota_vecino (vecino_id, estado);
CREATE INDEX idx_pago_fecha ON pago (fecha_pago, vecino_id);
CREATE INDEX idx_movimiento_fecha ON movimiento (fecha, tipo);
