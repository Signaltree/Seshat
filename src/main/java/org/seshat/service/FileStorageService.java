package org.seshat.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Map<String, String> MAGIC_BYTES = Map.of(
        "89504e47", "image/png",
        "ffd8ffe0", "image/jpeg",
        "ffd8ffe1", "image/jpeg",
        "ffd8ffe2", "image/jpeg",
        "25504446", "application/pdf",
        "47494638", "image/gif"
    );

    private final Path uploadDir;

    public FileStorageService(@Value("${seshat.upload-dir}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir.resolve("certificados"));
            Files.createDirectories(this.uploadDir.resolve("fotos"));
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear directorios de upload: " + this.uploadDir, e);
        }
    }

    public String guardar(MultipartFile archivo, String subdirectorio) {
        if (archivo.isEmpty()) throw new RuntimeException("Archivo vacío");
        validarMagicBytes(archivo);
        String extension = "";
        String original = archivo.getOriginalFilename();
        if (original != null && original.contains("."))
            extension = original.substring(original.lastIndexOf("."));
        String nombre = UUID.randomUUID() + extension;
        Path destino = uploadDir.resolve(subdirectorio).resolve(nombre).normalize();
        if (!destino.startsWith(uploadDir))
            throw new SecurityException("Ruta fuera del directorio permitido");
        try {
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return subdirectorio + "/" + nombre;
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar el archivo", e);
        }
    }

    private void validarMagicBytes(MultipartFile archivo) {
        try (InputStream is = archivo.getInputStream()) {
            byte[] header = new byte[4];
            int read = is.read(header);
            if (read < 4) throw new SecurityException("Archivo demasiado pequeño");
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 4; i++) hex.append(String.format("%02x", header[i]));
            if (!MAGIC_BYTES.containsKey(hex.toString())) {
                throw new SecurityException("Tipo de archivo no permitido. Solo PDF, JPEG, PNG y GIF");
            }
        } catch (SecurityException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("Error al leer archivo", e);
        }
    }

    public void eliminar(String rutaArchivo) {
        Path archivo = uploadDir.resolve(rutaArchivo).normalize();
        if (!archivo.startsWith(uploadDir))
            throw new SecurityException("Ruta fuera del directorio permitido");
        try {
            Files.deleteIfExists(archivo);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo eliminar el archivo", e);
        }
    }

    public Resource cargarComoResource(String rutaArchivo) {
        Path archivo = uploadDir.resolve(rutaArchivo).normalize();
        if (!archivo.startsWith(uploadDir))
            throw new SecurityException("Ruta fuera del directorio permitido");
        try {
            Resource r = new UrlResource(archivo.toUri());
            if (r.exists() && r.isReadable()) return r;
            throw new RuntimeException("Archivo no encontrado: " + rutaArchivo);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error al leer archivo", e);
        }
    }
}
