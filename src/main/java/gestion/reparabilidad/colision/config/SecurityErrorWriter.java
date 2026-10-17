package gestion.reparabilidad.colision.config;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class SecurityErrorWriter {

    private SecurityErrorWriter() {
    }

    public static void escribir(HttpServletResponse response, ObjectMapper objectMapper,
                                int status, String mensaje) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), Map.of("error", mensaje));
    }
}
