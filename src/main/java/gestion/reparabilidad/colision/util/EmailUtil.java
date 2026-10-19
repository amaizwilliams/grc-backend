package gestion.reparabilidad.colision.util;

public final class EmailUtil {

    private EmailUtil() {
    }

    public static String normalizar(String email) {
        return (email == null) ? null : email.trim().toLowerCase();
    }
}
