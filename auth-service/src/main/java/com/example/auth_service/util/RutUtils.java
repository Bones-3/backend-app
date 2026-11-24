package com.example.auth_service.util;

public final class RutUtils {
    private RutUtils() {
    }

    public static String normalizar(String rut) {
        return rut == null ? "" : rut.replaceAll("[^0-9Kk]", "").toUpperCase();
    }

    public static boolean esValido(String rut) {
        String limpio = normalizar(rut);
        if (limpio.length() < 8 || limpio.length() > 9) {
            return false;
        }

        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char digitoIngresado = limpio.charAt(limpio.length() - 1);
        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.digit(cuerpo.charAt(i), 10) * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }

        int resultado = 11 - (suma % 11);
        char digitoEsperado = resultado == 11 ? '0' : resultado == 10 ? 'K' : (char) ('0' + resultado);
        return digitoIngresado == digitoEsperado;
    }
}
