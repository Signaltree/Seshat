package org.seshat.controller;

import org.seshat.model.Foto;
import org.seshat.service.FotoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/fotos")
public class FotoController {

    private static final Logger log = LoggerFactory.getLogger(FotoController.class);
    private final FotoService service;

    public FotoController(FotoService service) { this.service = service; }

    @PostMapping("/subir")
    public String subir(@RequestParam int personaId,
                        @RequestParam(required = false) String descripcion,
                        @RequestParam(required = false) String fechaFoto,
                        @RequestParam MultipartFile archivo,
                        Model model) {
        LocalDate fecha = null;
        if (fechaFoto != null && !fechaFoto.isBlank()) {
            fecha = LocalDate.parse(fechaFoto);
        }
        try {
            service.guardar(personaId, descripcion, fecha, archivo);
        } catch (Exception e) {
            log.error("Error al subir foto: personaId={}", personaId, e);
            model.addAttribute("error", "Ocurrió un error al procesar la solicitud");
        }
        List<Foto> fotos = service.listarPorPersona(personaId);
        model.addAttribute("fotos", fotos);
        model.addAttribute("personaId", personaId);
        return "fragmentos/fotos :: fotos";
    }

    @GetMapping("/fragmento/{personaId}")
    public String fragmento(@PathVariable int personaId, Model model) {
        List<Foto> fotos = service.listarPorPersona(personaId);
        model.addAttribute("fotos", fotos);
        model.addAttribute("personaId", personaId);
        return "fragmentos/fotos :: fotos";
    }

    @GetMapping("/{id}/archivo")
    public ResponseEntity<Resource> ver(@PathVariable int id) {
        try {
            Foto f = service.buscarPorId(id);
            Resource r = service.cargarComoResource(id);
            String contentType = f.getTipoArchivo() != null ? f.getTipoArchivo() : "image/jpeg";
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .header("X-Content-Type-Options", "nosniff")
                    .body(r);
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable int id, @RequestParam int personaId, Model model) {
        try {
            service.eliminar(id);
        } catch (Exception e) {
            log.error("Error al eliminar foto: id={}, personaId={}", id, personaId, e);
            model.addAttribute("error", "Ocurrió un error al procesar la solicitud");
        }
        List<Foto> fotos = service.listarPorPersona(personaId);
        model.addAttribute("fotos", fotos);
        model.addAttribute("personaId", personaId);
        return "fragmentos/fotos :: fotos";
    }
}
