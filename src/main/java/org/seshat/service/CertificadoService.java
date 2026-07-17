package org.seshat.service;

import org.seshat.model.Certificado;
import org.seshat.repository.CertificadoRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
public class CertificadoService {

    private final CertificadoRepository repo;
    private final FileStorageService fileStorage;

    public CertificadoService(CertificadoRepository repo, FileStorageService fileStorage) {
        this.repo = repo;
        this.fileStorage = fileStorage;
    }

    public Certificado guardar(int personaId, String tipo, int entidadId, MultipartFile archivo) {
        String original = archivo.getOriginalFilename();
        if (original == null || original.isBlank()) original = "sin_nombre";
        String extension = "";
        if (original.contains(".")) extension = original.substring(original.lastIndexOf("."));
        String tipoArchivo = switch (extension.toLowerCase()) {
            case ".pdf" -> "application/pdf";
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            default -> "application/octet-stream";
        };
        String ruta = fileStorage.guardar(archivo, "certificados");
        Certificado c = new Certificado(0, personaId, tipo, entidadId, original, ruta, tipoArchivo, LocalDate.now());
        int id = repo.save(c);
        c.setId(id);
        return c;
    }

    public List<Certificado> listarPorEntidad(String tipo, int entidadId) {
        return repo.findByTipoAndEntidadId(tipo, entidadId);
    }

    public Certificado buscarPorId(int id) {
        return repo.findById(id);
    }

    public void eliminar(int id) {
        Certificado c = repo.findById(id);
        repo.delete(id);
        fileStorage.eliminar(c.getRutaArchivo());
    }

    public Resource cargarComoResource(int id) {
        Certificado c = repo.findById(id);
        return fileStorage.cargarComoResource(c.getRutaArchivo());
    }
}
