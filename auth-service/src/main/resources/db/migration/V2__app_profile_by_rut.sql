ALTER TABLE credencial ADD COLUMN rut_user VARCHAR(12) NULL;
ALTER TABLE credencial ADD COLUMN nombre VARCHAR(120) NULL;
ALTER TABLE credencial ADD COLUMN telefono VARCHAR(30) NULL;
ALTER TABLE credencial ADD COLUMN region VARCHAR(80) NULL;
ALTER TABLE credencial ADD COLUMN comuna VARCHAR(80) NULL;

CREATE UNIQUE INDEX uk_credencial_rut_user ON credencial (rut_user);

UPDATE credencial
SET rut_user = '111111111',
    nombre = 'Vecino Demo',
    telefono = '911111111',
    region = 'Región Metropolitana',
    comuna = 'Santiago',
    contrasena_user = '$2a$10$q1B8RFuAupJa4KaG7rDWI.R8dlkKKzacDSYkRWc0zvrj41n6CSaiO'
WHERE correo_user = 'vcortez6565@gmail.com';

INSERT INTO credencial (
    correo_user, rut_user, nombre, telefono, region, comuna,
    contrasena_user, activo, rol
)
SELECT
    '222222222@rut.local', '222222222', 'Vecino Demo', '922222222',
    'Región Metropolitana', 'Ñuñoa',
    '$2a$10$3SznHuO/OYZPYNPJce3prOwkXvOxKXMAFIypNLkfm7Lrn5u4Q15R6', TRUE, 'VECINO'
WHERE NOT EXISTS (
    SELECT 1 FROM credencial WHERE rut_user = '222222222'
);
