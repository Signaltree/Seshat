package org.seshat.util;

import java.util.regex.Pattern;

public class ValidacionUtil {

    private static final Pattern RUT = Pattern.compile("^\\d{1,2}\\.\\d{3}\\.\\d{3}[-][0-9kK]$");
    private static final Pattern RUT_SIMPLE = Pattern.compile("^(\\d{7,8})([0-9kK])$");
    private static final Pattern EMAIL = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern TELEFONO = Pattern.compile("^(\\+56\\s?)?(9\\s?)?\\d{4}\\s?\\d{4}$");

    public static String limpiarRut(String rut) {
        if (rut == null) return null;
        return rut.replaceAll("[^\\dkK]", "");
    }

    public static boolean validarRut(String rut) {
        if (rut == null || rut.isBlank()) return true;
        String limpio = limpiarRut(rut);
        if (limpio.length() < 8 || limpio.length() > 9) return false;
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char dv = limpio.charAt(limpio.length() - 1);
        return Character.toUpperCase(dv) == calcularDigitoVerificador(Integer.parseInt(cuerpo));
    }

    public static boolean validarEmail(String email) {
        return email == null || email.isBlank() || EMAIL.matcher(email).matches();
    }

    public static boolean validarTelefono(String telefono) {
        return telefono == null || telefono.isBlank() || TELEFONO.matcher(telefono).matches();
    }

    private static char calcularDigitoVerificador(int rut) {
        int suma = 0;
        int multiplicador = 2;
        while (rut > 0) {
            suma += (rut % 10) * multiplicador;
            rut /= 10;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }
        int resto = suma % 11;
        int dv = 11 - resto;
        if (dv == 11) return '0';
        if (dv == 10) return 'K';
        return (char) ('0' + dv);
    }
}
