package org.seshat.controller;

import org.seshat.model.Matrimonio;
import org.seshat.service.MatrimonioService;
import org.seshat.service.PersonaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/matrimonios")
public class MatrimonioController {
    private static final Logger log = LoggerFactory.getLogger(MatrimonioController.class);
    private final MatrimonioService service;
    private final PersonaService personaService;

    public MatrimonioController(MatrimonioService service, PersonaService personaService) {
        this.service = service;
        this.personaService = personaService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("matrimonios", q != null && !q.isBlank() ? service.buscar(q) : service.listar());
        model.addAttribute("q", q);
        return "matrimonios/listar";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("matrimonio", new Matrimonio());
        model.addAttribute("personas", personaService.listar());
        return "matrimonios/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam(defaultValue = "0") int id, Matrimonio m, Model model) {
        try {
            if (id > 0) { m.setId(id); service.actualizar(m); }
            else service.guardar(m);
        } catch (Exception e) {
            log.error("Error al guardar matrimonio: id={}", id, e);
            model.addAttribute("error", "Ocurrió un error al procesar la solicitud");
            model.addAttribute("matrimonio", m);
            model.addAttribute("personas", personaService.listar());
            return "matrimonios/formulario";
        }
        model.addAttribute("matrimonios", service.listar());
        return "matrimonios/listar";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable int id, Model model) {
        model.addAttribute("matrimonio", service.obtenerPorId(id));
        model.addAttribute("personas", personaService.listar());
        return "matrimonios/formulario";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable int id, Model model) {
        try {
            service.eliminar(id);
        } catch (DataIntegrityViolationException e) {
            log.error("Error al eliminar matrimonio: id={}, violación de integridad", id, e);
            model.addAttribute("error", "No se puede eliminar: el matrimonio tiene registros asociados.");
        } catch (Exception e) {
            log.error("Error al eliminar matrimonio: id={}", id, e);
            model.addAttribute("error", "Ocurrió un error al procesar la solicitud");
        }
        model.addAttribute("matrimonios", service.listar());
        return "matrimonios/listar";
    }
}
