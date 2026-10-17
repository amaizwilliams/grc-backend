package gestion.reparabilidad.colision.util;

public final class CelularUtil {

    private CelularUtil() {
    }

    public static String normalizar(String celular) {
        String limpio = celular.trim().replaceAll("[ -]", "");

        if (limpio.startsWith("+")) {
            return limpio;
        }
        if (limpio.length() == 10) {
            return "+57" + limpio;
        }
        return "+" + limpio;
    }
}