package gestion.reparabilidad.colision.util;

public final class PlacaUtil {

    private PlacaUtil() {
    }

    // " abc-123 " -> "ABC123": la base guarda la placa sin espacios ni guion, en mayúsculas
    public static String normalizar(String placa) {
        return placa.replaceAll("[\\s-]", "").toUpperCase();
    }
}
