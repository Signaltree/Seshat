package org.seshat.controller;

import org.seshat.model.Certificado;
import org.seshat.service.CertificadoService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/certificados")
public class CertificadoController {

    private final CertificadoService service;

    public CertificadoController(CertificadoService service) { this.service = service; }

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
        Certificado c = service.buscarPorId(id);
        Resource r = service.cargarComoResource(id);
        String contentType = c.getTipoArchivo() != null ? c.getTipoArchivo() : "application/octet-stream";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + c.getNombreOriginal() + "\"")
                .body(r);
    }

    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable int id, @RequestParam String tipo,
                           @RequestParam int entidadId, @RequestParam int personaId,
                           Model model) {
        try {
            service.eliminar(id);
        } catch (Exception e) {
            model.addAttribute("error", "Error al eliminar archivo");
        }
        List<Certificado> certificados = service.listarPorEntidad(tipo, entidadId);
        model.addAttribute("certificados", certificados);
        model.addAttribute("tipo", tipo);
        model.addAttribute("entidadId", entidadId);
        model.addAttribute("personaId", personaId);
        return "fragmentos/certificados :: certificados";
    }
}
