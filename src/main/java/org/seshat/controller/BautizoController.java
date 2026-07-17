package org.seshat.controller;

import org.seshat.model.Bautizo;
import org.seshat.service.BautizoService;
import org.seshat.service.PersonaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/bautizos")
public class BautizoController {
    private static final Logger log = LoggerFactory.getLogger(BautizoController.class);
    private final BautizoService service;
    private final PersonaService personaService;

    public BautizoController(BautizoService service, PersonaService personaService) {
        this.service = service;
        this.personaService = personaService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("bautizos", q != null && !q.isBlank() ? service.buscar(q) : service.listar());
        model.addAttribute("q", q);
        return "bautizos/listar";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("bautizo", new Bautizo());
        model.addAttribute("personas", personaService.listar());
        return "bautizos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam(defaultValue = "0") int id, Bautizo b, Model model) {
        try {
            if (id > 0) { b.setId(id); service.actualizar(b); }
            else service.guardar(b);
        } catch (Exception e) {
            log.error("Error al guardar bautizo: id={}", id, e);
            model.addAttribute("error", "Ocurrió un error al procesar la solicitud");
            model.addAttribute("bautizo", b);
            model.addAttribute("personas", personaService.listar());
            return "bautizos/formulario";
        }
        model.addAttribute("bautizos", service.listar());
        return "bautizos/listar";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable int id, Model model) {
        model.addAttribute("bautizo", service.obtenerPorId(id));
        model.addAttribute("personas", personaService.listar());
        return "bautizos/formulario";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable int id, Model model) {
        try {
            service.eliminar(id);
        } catch (DataIntegrityViolationException e) {
            log.error("Error al eliminar bautizo: id={}, violación de integridad", id, e);
            model.addAttribute("error", "No se puede eliminar: el bautizo tiene registros asociados.");
        } catch (Exception e) {
            log.error("Error al eliminar bautizo: id={}", id, e);
            model.addAttribute("error", "Ocurrió un error al procesar la solicitud");
        }
        model.addAttribute("bautizos", service.listar());
        return "bautizos/listar";
    }
}
