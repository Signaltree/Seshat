package org.seshat.controller;

import org.seshat.model.Foto;
import org.seshat.service.FotoService;
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
            model.addAttribute("error", "Error al subir foto: " + e.getMessage());
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
            model.addAttribute("error", "Error al eliminar foto: " + e.getMessage());
        }
        List<Foto> fotos = service.listarPorPersona(personaId);
        model.addAttribute("fotos", fotos);
        model.addAttribute("personaId", personaId);
        return "fragmentos/fotos :: fotos";
    }
}
