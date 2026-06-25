package org.seshat.service;

import org.seshat.model.Persona;
import org.seshat.repository.PersonaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PersonaService {
    private final PersonaRepository repo;

    public PersonaService(PersonaRepository repo) {
        this.repo = repo;
    }

    public List<Persona> listar() { return repo.findAll(); }

    public Persona obtenerPorId(int id) { return repo.findById(id); }

    public int guardar(Persona p) {
        if (p.getFecha_registro() == null) p.setFecha_registro(LocalDate.now());
        return repo.save(p);
    }

    public void actualizar(Persona p) { repo.update(p); }

    public void eliminar(int id) { repo.delete(id); }
}
