package gestion.reparabilidad.colision.service;
import gestion.reparabilidad.colision.dto.tecnico.TecnicoUpdateDto;
import gestion.reparabilidad.colision.exception.RecursoNoEncontradoException;
import gestion.reparabilidad.colision.exception.ReglaNegocioException;
import gestion.reparabilidad.colision.modelo.Tecnico;
import gestion.reparabilidad.colision.repository.TecnicoRepository;
import gestion.reparabilidad.colision.dto.tecnico.TecnicoRequestDto;
import gestion.reparabilidad.colision.util.CelularUtil;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

import java.util.List;

@Service
public class TecnicoService {

    private final TecnicoRepository tecnicoRepository;

    public TecnicoService(TecnicoRepository tecnicoRepository) {
        this.tecnicoRepository = tecnicoRepository;
    }

    public List<Tecnico> getAllTecnico() {
        return tecnicoRepository.findAll();
    }

    public List<Tecnico> getAllTecnicoDesactivado() {
        return tecnicoRepository.findByActivoFalse();
    }

    public Tecnico getTecnicoById(Long id) {
        return tecnicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el técnico con id " + id));
    }

    public Tecnico saveTecnico(TecnicoRequestDto request) {
        String documento = request.getDocumento().trim();
        if(tecnicoRepository.existsByDocumento(documento)){
            throw new ReglaNegocioException("Ya existe un técnico con el documento " + documento);
        }
        Tecnico tecnico = new Tecnico();
        tecnico.setDocumento(documento);
        tecnico.setNombreCompleto(request.getNombreCompleto().trim());
        tecnico.setCelular(CelularUtil.normalizar(request.getCelular()));
        tecnico.setCorreo(request.getCorreo());
        tecnico.setEspecialidad(request.getEspecialidad());
        LocalDateTime ahora = LocalDateTime.now();
        tecnico.setActivo(true);
        tecnico.setCreateAt(ahora);
        tecnico.setUpdateAt(ahora);
        return tecnicoRepository.save(tecnico);
    }

    public Tecnico updateTecnico(Long id ,TecnicoUpdateDto datos) {
        Tecnico tecnico = getTecnicoById(id);
        tecnico.setNombreCompleto(datos.getNombreCompleto().trim());
        tecnico.setCelular(CelularUtil.normalizar(datos.getCelular()));
        tecnico.setCorreo(datos.getCorreo());
        tecnico.setEspecialidad(datos.getEspecialidad());
        tecnico.setUpdateAt(LocalDateTime.now());
        return tecnicoRepository.save(tecnico);
    }

    public void deleteTecnicoById(Long id) {
        Tecnico tecnico = getTecnicoById(id);
        tecnico.setActivo(false);
        tecnico.setUpdateAt(LocalDateTime.now());
        tecnicoRepository.save(tecnico);
    }

    public Tecnico reactivarTecnico(Long id){
        Tecnico tecnico = getTecnicoById(id);
        tecnico.setActivo(true);
        tecnico.setUpdateAt(LocalDateTime.now());
        return tecnicoRepository.save(tecnico);
    }


}
