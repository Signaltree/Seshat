package org.seshat.service;

import org.seshat.model.Persona;
import org.seshat.repository.PersonaRepository;
import org.seshat.util.ValidacionUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PersonaService {
    private final PersonaRepository repo;

    public PersonaService(PersonaRepository repo) {
        this.repo = repo;
    }

    public List<Persona> listar() { return repo.findAll(); }

    public Persona obtenerPorId(int id) { return repo.findById(id); }

    public Map<String, String> validar(Persona p) {
        Map<String, String> errores = new HashMap<>();
        if (p.getRut() != null && !p.getRut().isBlank() && !ValidacionUtil.validarRut(p.getRut()))
            errores.put("rut", "RUT inválido (use formato: XX.XXX.XXX-X)");
        if (p.getEmail() != null && !p.getEmail().isBlank() && !ValidacionUtil.validarEmail(p.getEmail()))
            errores.put("email", "Email inválido");
        if (p.getTelefono() != null && !p.getTelefono().isBlank() && !ValidacionUtil.validarTelefono(p.getTelefono()))
            errores.put("telefono", "Teléfono inválido (use formato: +56 9 XXXX XXXX)");
        if (p.getNombres() != null && p.getNombres().length() > 100)
            errores.put("nombres", "El nombre no puede exceder 100 caracteres");
        if (p.getApellidos() != null && p.getApellidos().length() > 100)
            errores.put("apellidos", "Los apellidos no pueden exceder 100 caracteres");
        if (p.getRut() != null && p.getRut().length() > 20)
            errores.put("rut", "El RUT no puede exceder 20 caracteres");
        if (p.getDireccion() != null && p.getDireccion().length() > 255)
            errores.put("direccion", "La dirección no puede exceder 255 caracteres");
        if (p.getTelefono() != null && p.getTelefono().length() > 20)
            errores.put("telefono", "El teléfono no puede exceder 20 caracteres");
        if (p.getEmail() != null && p.getEmail().length() > 100)
            errores.put("email", "El email no puede exceder 100 caracteres");
        return errores;
    }

    public int guardar(Persona p) {
        if (p.getFecha_registro() == null) p.setFecha_registro(LocalDate.now());
        return repo.save(p);
    }

    public void actualizar(Persona p) { repo.update(p); }

    public void eliminar(int id) { repo.delete(id); }
}
