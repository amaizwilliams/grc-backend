package gestion.reparabilidad.colision.dto;

import gestion.reparabilidad.colision.dto.usuario.UsuarioResponseDto;

public record LoginResponse(String token, UsuarioResponseDto usuario) {
}
