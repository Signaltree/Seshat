package org.seshat.service;

import org.seshat.model.Bautizo;
import org.seshat.repository.BautizoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BautizoService {
    private final BautizoRepository repo;

    public BautizoService(BautizoRepository repo) { this.repo = repo; }

    public List<Bautizo> listar() { return repo.findAll(); }
    public Bautizo obtenerPorId(int id) { return repo.findById(id); }
    public int guardar(Bautizo b) { return repo.save(b); }
    public void actualizar(Bautizo b) { repo.update(b); }
    public void eliminar(int id) { repo.delete(id); }
}
