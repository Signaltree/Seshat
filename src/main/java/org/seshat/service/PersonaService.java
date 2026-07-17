package org.seshat.service;

import org.seshat.model.Persona;
import org.seshat.repository.PersonaRepository;
import org.seshat.util.ValidacionUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PersonaService {
    private final PersonaRepository repo;
    private final JdbcTemplate jdbc;
    private final FileStorageService fileStorage;

    public PersonaService(PersonaRepository repo, JdbcTemplate jdbc, FileStorageService fileStorage) {
        this.repo = repo;
        this.jdbc = jdbc;
        this.fileStorage = fileStorage;
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

    public List<Persona> buscar(String q) { return repo.findByQuery(q); }

    public int guardar(Persona p) {
        if (p.getFecha_registro() == null) p.setFecha_registro(LocalDate.now());
        return repo.save(p);
    }

    public void actualizar(Persona p) { repo.update(p); }

    @Transactional
    public void eliminar(int id) {
        var rutasCertificados = jdbc.queryForList(
            "SELECT ruta_archivo FROM CERTIFICADO WHERE persona_id = ?", String.class, id);
        var rutasFotos = jdbc.queryForList(
            "SELECT ruta_archivo FROM FOTO WHERE persona_id = ?", String.class, id);
        jdbc.update("DELETE FROM BAUTIZO_PADRINO WHERE bautizo_id IN (SELECT id FROM BAUTIZO WHERE persona_id = ?)", id);
        jdbc.update("DELETE FROM CONFIRMACION_PADRINO WHERE confirmacion_id IN (SELECT id FROM CONFIRMACION WHERE persona_id = ?)", id);
        jdbc.update("DELETE FROM MATRIMONIO_PADRINO WHERE matrimonio_id IN (SELECT id FROM MATRIMONIO WHERE persona1_id = ? OR persona2_id = ?)", id, id);
        jdbc.update("DELETE FROM CERTIFICADO WHERE persona_id = ?", id);
        jdbc.update("DELETE FROM BAUTIZO WHERE persona_id = ?", id);
        jdbc.update("DELETE FROM CONFIRMACION WHERE persona_id = ?", id);
        jdbc.update("DELETE FROM MATRIMONIO WHERE persona1_id = ? OR persona2_id = ?", id, id);
        jdbc.update("DELETE FROM FOTO WHERE persona_id = ?", id);
        repo.delete(id);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                rutasCertificados.forEach(r -> fileStorage.eliminar(r));
                rutasFotos.forEach(r -> fileStorage.eliminar(r));
            }
        });
    }
}
