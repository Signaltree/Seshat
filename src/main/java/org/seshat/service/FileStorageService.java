package org.seshat.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

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
        String extension = "";
        String original = archivo.getOriginalFilename();
        if (original != null && original.contains("."))
            extension = original.substring(original.lastIndexOf("."));
        String nombre = UUID.randomUUID() + extension;
        Path destino = uploadDir.resolve(subdirectorio).resolve(nombre);
        try {
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return subdirectorio + "/" + nombre;
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar el archivo", e);
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
