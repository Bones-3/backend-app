CREATE TABLE vecino (
    id_vecino BIGINT AUTO_INCREMENT PRIMARY KEY,
    rut VARCHAR(12) NOT NULL UNIQUE,
    correo_vecino VARCHAR(90) NOT NULL UNIQUE,
    nombres VARCHAR(90) NOT NULL,
    apellidos VARCHAR(90) NOT NULL,
    telefono_vecino VARCHAR(30),
    estado_vecino BOOLEAN NOT NULL DEFAULT TRUE,
    rol VARCHAR(20) NOT NULL DEFAULT 'VECINO'
);

INSERT INTO vecino (
    rut, correo_vecino, nombres, apellidos, telefono_vecino, estado_vecino, rol
) VALUES
('11111111-1', 'directiva@juntavecinal.cl', 'Andrea', 'Directiva', '+56911111111', TRUE, 'DIRECTIVA'),
('22222222-2', 'tesorero@juntavecinal.cl', 'Tomás', 'Tesorero', '+56922222222', TRUE, 'TESORERO'),
('33333333-3', 'secretario@juntavecinal.cl', 'Sergio', 'Secretario', '+56933333333', TRUE, 'SECRETARIO'),
('44444444-4', 'vecino1@juntavecinal.cl', 'Ana', 'Morales', '+56944444444', TRUE, 'VECINO'),
('55555555-5', 'vecino2@juntavecinal.cl', 'Pedro', 'Silva', '+56955555555', TRUE, 'VECINO');
