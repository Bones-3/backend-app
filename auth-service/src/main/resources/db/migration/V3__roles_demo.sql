UPDATE credencial SET rol = 'DIRECTIVA' WHERE rut_user = '111111111';

INSERT INTO credencial (correo_user, rut_user, nombre, contrasena_user, activo, rol)
SELECT '333333333@rut.local', '333333333', 'Tesorería Demo', '$2a$10$G/rtl5.pwO0xRTx3rMK/yu84gQIDeOcHKmFYHrnAd4QH26HqjRpge', TRUE, 'TESORERO'
WHERE NOT EXISTS (SELECT 1 FROM credencial WHERE rut_user = '333333333');

INSERT INTO credencial (correo_user, rut_user, nombre, contrasena_user, activo, rol)
SELECT '444444444@rut.local', '444444444', 'Secretaría Demo', '$2a$10$4bfFEGsQ/Jt9cGoH2YgdVOTU9L/lqYDlb0ppfk7nGIXU83EuQbfh2', TRUE, 'SECRETARIO'
WHERE NOT EXISTS (SELECT 1 FROM credencial WHERE rut_user = '444444444');
