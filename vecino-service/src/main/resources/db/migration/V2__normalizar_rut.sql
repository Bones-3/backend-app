UPDATE vecino
SET rut = REPLACE(REPLACE(UPPER(rut), '.', ''), '-', '');
