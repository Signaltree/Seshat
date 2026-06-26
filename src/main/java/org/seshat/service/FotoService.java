package org.seshat.service;

import org.seshat.model.Foto;
import org.seshat.repository.FotoRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
public class FotoService {

    private final FotoRepository repo;
    private final FileStorageService fileStorage;

    public FotoService(FotoRepository repo, FileStorageService fileStorage) {
        this.repo = repo;
        this.fileStorage = fileStorage;
    }

    public Foto guardar(int personaId, String descripcion, LocalDate fechaFoto, MultipartFile archivo) {
        String original = archivo.getOriginalFilename();
        if (original == null || original.isBlank()) original = "sin_nombre";
        String extension = "";
        if (original.contains(".")) extension = original.substring(original.lastIndexOf("."));
        String tipoArchivo = switch (extension.toLowerCase()) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            default -> "application/octet-stream";
        };
        String ruta = fileStorage.guardar(archivo, "fotos");
        Foto f = new Foto(0, personaId, descripcion, ruta, tipoArchivo, LocalDate.now(), fechaFoto);
        int id = repo.save(f);
        f.setId(id);
        return f;
    }

    public List<Foto> listarPorPersona(int personaId) {
        return repo.findByPersonaId(personaId);
    }

    public Foto buscarPorId(int id) {
        return repo.findById(id);
    }

    public void eliminar(int id) {
        Foto f = repo.findById(id);
        fileStorage.eliminar(f.getRutaArchivo());
        repo.delete(id);
    }

    public Resource cargarComoResource(int id) {
        Foto f = repo.findById(id);
        return fileStorage.cargarComoResource(f.getRutaArchivo());
    }
}
