package gestion.reparabilidad.colision.service;

import gestion.reparabilidad.colision.dto.etapas.EtapasRequestDto;
import gestion.reparabilidad.colision.dto.etapas.EtapasResponseDto;
import gestion.reparabilidad.colision.exception.RecursoNoEncontradoException;
import gestion.reparabilidad.colision.exception.ReglaNegocioException;
import gestion.reparabilidad.colision.modelo.Etapas;
import gestion.reparabilidad.colision.repository.EtapasRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EtapasService {

    private final EtapasRepository etapasRepository;

    public EtapasService(EtapasRepository etapasRepository) {
        this.etapasRepository = etapasRepository;
    }

    public List<EtapasResponseDto> getAllEtapas() {
        return etapasRepository.findAllByOrderByOrdenAsc().stream().map(this::convertirADto).toList();
    }

    public EtapasResponseDto getEtapaById(Long id) {
        return convertirADto(buscarEtapa(id));
    }

    public EtapasResponseDto saveEtapa(EtapasRequestDto request) {
        String nombre = request.nombre().trim();
        if (etapasRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("Ya existe una etapa con el nombre " + nombre);
        }
        if (etapasRepository.existsByOrden(request.orden())) {
            throw new ReglaNegocioException("Ya existe una etapa en la posición " + request.orden());
        }
        Etapas etapa = new Etapas();
        copiarDatos(etapa, request, nombre);
        return convertirADto(etapasRepository.save(etapa));
    }

    public EtapasResponseDto updateEtapa(Long id, EtapasRequestDto request) {
        Etapas etapa = buscarEtapa(id);
        String nombre = request.nombre().trim();
        if (etapasRepository.existsByNombreIgnoreCaseAndIdEtapaNot(nombre, id)) {
            throw new ReglaNegocioException("Ya existe una etapa con el nombre " + nombre);
        }
        if (etapasRepository.existsByOrdenAndIdEtapaNot(request.orden(), id)) {
            throw new ReglaNegocioException("Ya existe una etapa en la posición " + request.orden());
        }
        copiarDatos(etapa, request, nombre);
        return convertirADto(etapasRepository.save(etapa));
    }

    public Etapas buscarEtapa(Long id) {
        return etapasRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la etapa con id " + id));
    }

    private void copiarDatos(Etapas etapa, EtapasRequestDto request, String nombre) {
        etapa.setNombre(nombre);
        etapa.setOrden(request.orden());
        etapa.setDescripcion(request.descripcion());
        etapa.setDiasLimiteCritico(request.diasLimiteCritico());
        etapa.setEspecialidad(request.especialidad());
    }

    private EtapasResponseDto convertirADto(Etapas etapa) {
        return new EtapasResponseDto(
                etapa.getIdEtapa(),
                etapa.getNombre(),
                etapa.getOrden(),
                etapa.getDescripcion(),
                etapa.getDiasLimiteCritico(),
                etapa.getEspecialidad()
        );
    }
}
