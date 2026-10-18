package gestion.reparabilidad.colision.util;

public final class DocumentoUtil {

    private DocumentoUtil() {
    }

    // " 1.017.171.717 " -> "1017171717": la base guarda el documento solo con dígitos
    public static String normalizar(String documento) {
        return documento.replaceAll("[\\s.]", "");
    }
}
