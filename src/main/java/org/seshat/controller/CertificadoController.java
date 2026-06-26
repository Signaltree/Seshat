package org.seshat.controller;

import org.seshat.model.Certificado;
import org.seshat.service.CertificadoService;
import org.seshat.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/certificados")
public class CertificadoController {

    private final CertificadoService service;
    private final FileStorageService fileStorage;

    public CertificadoController(CertificadoService service, FileStorageService fileStorage) {
        this.service = service;
        this.fileStorage = fileStorage;
    }

    @PostMapping("/subir")
    public String subir(@RequestParam int personaId, @RequestParam String tipo,
                        @RequestParam int entidadId, @RequestParam MultipartFile archivo,
                        Model model) {
        try {
            service.guardar(personaId, tipo, entidadId, archivo);
        } catch (Exception e) {
            model.addAttribute("error", "Error al subir archivo: " + e.getMessage());
        }
        List<Certificado> certificados = service.listarPorEntidad(tipo, entidadId);
        model.addAttribute("certificados", certificados);
        model.addAttribute("tipo", tipo);
        model.addAttribute("entidadId", entidadId);
        model.addAttribute("personaId", personaId);
        return "fragmentos/certificados :: certificados";
    }

    @GetMapping("/fragmento/{tipo}/{entidadId}")
    public String fragmento(@PathVariable String tipo, @PathVariable int entidadId,
                            @RequestParam int personaId, Model model) {
        List<Certificado> certificados = service.listarPorEntidad(tipo, entidadId);
        model.addAttribute("certificados", certificados);
        model.addAttribute("tipo", tipo);
        model.addAttribute("entidadId", entidadId);
        model.addAttribute("personaId", personaId);
        return "fragmentos/certificados :: certificados";
    }

    @GetMapping("/{id}/archivo")
    public ResponseEntity<Resource> descargar(@PathVariable int id) {
        try {
            Certificado c = service.buscarPorId(id);
            Resource r = fileStorage.cargarComoResource(c.getRutaArchivo());
            String contentType = c.getTipoArchivo() != null ? c.getTipoArchivo() : "application/octet-stream";
            String filename = URLEncoder.encode(c.getNombreOriginal(), StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename*=UTF-8''" + filename)
                    .body(r);
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable int id, @RequestParam String tipo,
                           @RequestParam int entidadId, @RequestParam int personaId,
                           Model model) {
        try {
            service.eliminar(id);
        } catch (Exception e) {
            model.addAttribute("error", "Error al eliminar archivo: " + e.getMessage());
        }
        List<Certificado> certificados = service.listarPorEntidad(tipo, entidadId);
        model.addAttribute("certificados", certificados);
        model.addAttribute("tipo", tipo);
        model.addAttribute("entidadId", entidadId);
        model.addAttribute("personaId", personaId);
        return "fragmentos/certificados :: certificados";
    }
}
