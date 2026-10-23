package gestion.reparabilidad.colision.service;
import gestion.reparabilidad.colision.dto.tecnico.TecnicoResponseDto;
import gestion.reparabilidad.colision.dto.tecnico.TecnicoUpdateDto;
import gestion.reparabilidad.colision.exception.RecursoNoEncontradoException;
import gestion.reparabilidad.colision.exception.ReglaNegocioException;
import gestion.reparabilidad.colision.modelo.Enums.Especialidad;
import gestion.reparabilidad.colision.modelo.Tecnico;
import gestion.reparabilidad.colision.repository.TecnicoRepository;
import gestion.reparabilidad.colision.dto.tecnico.TecnicoRequestDto;
import gestion.reparabilidad.colision.util.CelularUtil;
import gestion.reparabilidad.colision.util.DocumentoUtil;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

import java.util.List;

@Service
public class TecnicoService {

    private final TecnicoRepository tecnicoRepository;

    public TecnicoService(TecnicoRepository tecnicoRepository) {
        this.tecnicoRepository = tecnicoRepository;
    }

    public List<TecnicoResponseDto> getAllTecnico() {
        return tecnicoRepository.findByActivoTrue().stream().map(this::convertirADto).toList();
    }

    public List<TecnicoResponseDto> getAllTecnicoDesactivado() {
        return tecnicoRepository.findByActivoFalse().stream().map(this::convertirADto).toList();
    }

    public List<TecnicoResponseDto> getTecnicosByEspecialidad(Especialidad especialidad) {
        return tecnicoRepository.findByActivoTrueAndEspecialidad(especialidad).stream().map(this::convertirADto).toList();
    }

    public TecnicoResponseDto getTecnicoById(Long id) {
        return convertirADto(buscarTecnico(id));
    }

    public TecnicoResponseDto saveTecnico(TecnicoRequestDto request) {
        String documento = DocumentoUtil.normalizar(request.documento());
        if(tecnicoRepository.existsByDocumento(documento)){
            throw new ReglaNegocioException("Ya existe un técnico con el documento " + documento);
        }
        Tecnico tecnico = new Tecnico();
        tecnico.setDocumento(documento);
        tecnico.setNombreCompleto(request.nombreCompleto().trim());
        tecnico.setCelular(CelularUtil.normalizar(request.celular()));
        tecnico.setCorreo(request.correo());
        tecnico.setEspecialidad(request.especialidad());
        tecnico.setActivo(true);
        tecnico.setCreateAt(LocalDateTime.now());
        return convertirADto(tecnicoRepository.save(tecnico));
    }

    public TecnicoResponseDto updateTecnico(Long id ,TecnicoUpdateDto datos) {
        Tecnico tecnico = buscarTecnico(id);
        tecnico.setNombreCompleto(datos.nombreCompleto().trim());
        tecnico.setCelular(CelularUtil.normalizar(datos.celular()));
        tecnico.setCorreo(datos.correo());
        tecnico.setEspecialidad(datos.especialidad());
        tecnico.setUpdateAt(LocalDateTime.now());
        return convertirADto(tecnicoRepository.save(tecnico));
    }

    public void deleteTecnicoById(Long id) {
        Tecnico tecnico = buscarTecnico(id);
        tecnico.setActivo(false);
        tecnico.setUpdateAt(LocalDateTime.now());
        tecnicoRepository.save(tecnico);
    }

    public TecnicoResponseDto reactivarTecnico(Long id){
        Tecnico tecnico = buscarTecnico(id);
        tecnico.setActivo(true);
        tecnico.setUpdateAt(LocalDateTime.now());
        return convertirADto(tecnicoRepository.save(tecnico));
    }

    public Tecnico buscarTecnico(Long id) {
        return tecnicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el técnico con id " + id));
    }

    private TecnicoResponseDto convertirADto(Tecnico tecnico) {
        return new TecnicoResponseDto(
                tecnico.getIdPersona(),
                tecnico.getDocumento(),
                tecnico.getNombreCompleto(),
                tecnico.getCelular(),
                tecnico.getCorreo(),
                tecnico.getEspecialidad(),
                tecnico.getActivo(),
                tecnico.getCreateAt(),
                tecnico.getUpdateAt()
        );
    }

}
