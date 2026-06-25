package org.seshat.service;

import org.seshat.model.Confirmacion;
import org.seshat.repository.ConfirmacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfirmacionService {
    private final ConfirmacionRepository repo;

    public ConfirmacionService(ConfirmacionRepository repo) { this.repo = repo; }

    public List<Confirmacion> listar() { return repo.findAll(); }
    public Confirmacion obtenerPorId(int id) { return repo.findById(id); }
    public int guardar(Confirmacion c) { return repo.save(c); }
    public void actualizar(Confirmacion c) { repo.update(c); }
    public void eliminar(int id) { repo.delete(id); }
}
