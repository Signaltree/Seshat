package org.seshat.service;

import org.seshat.model.Matrimonio;
import org.seshat.repository.MatrimonioRepository;
import org.seshat.repository.PadrinoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MatrimonioService {
    private final MatrimonioRepository repo;
    private final PadrinoRepository padrinoRepo;

    public MatrimonioService(MatrimonioRepository repo, PadrinoRepository padrinoRepo) {
        this.repo = repo;
        this.padrinoRepo = padrinoRepo;
    }

    public List<Matrimonio> listar() { return repo.findAll(); }
    public List<Matrimonio> buscar(String q) { return repo.findByQuery(q); }
    public Matrimonio obtenerPorId(int id) { return repo.findById(id); }
    public int guardar(Matrimonio m) { return repo.save(m); }
    public void actualizar(Matrimonio m) { repo.update(m); }
    @Transactional
    public void eliminar(int id) {
        padrinoRepo.eliminarMatrimonioPadrinosPorMatrimonio(id);
        repo.delete(id);
    }
}
