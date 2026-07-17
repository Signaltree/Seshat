package org.seshat.service;

import org.seshat.model.Confirmacion;
import org.seshat.repository.ConfirmacionRepository;
import org.seshat.repository.PadrinoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConfirmacionService {
    private final ConfirmacionRepository repo;
    private final PadrinoRepository padrinoRepo;

    public ConfirmacionService(ConfirmacionRepository repo, PadrinoRepository padrinoRepo) {
        this.repo = repo;
        this.padrinoRepo = padrinoRepo;
    }

    public List<Confirmacion> listar() { return repo.findAll(); }
    public List<Confirmacion> buscar(String q) { return repo.findByQuery(q); }
    public Confirmacion obtenerPorId(int id) { return repo.findById(id); }
    public int guardar(Confirmacion c) { return repo.save(c); }
    public void actualizar(Confirmacion c) { repo.update(c); }
    @Transactional
    public void eliminar(int id) {
        padrinoRepo.eliminarConfirmacionPadrinosPorConfirmacion(id);
        repo.delete(id);
    }
}
