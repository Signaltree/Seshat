package org.seshat.controller;

import org.seshat.model.Persona;
import org.seshat.service.PersonaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/personas")
public class PersonaController {
    private static final Logger log = LoggerFactory.getLogger(PersonaController.class);
    private final PersonaService service;

    public PersonaController(PersonaService service) { this.service = service; }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("personas", service.listar());
        return "personas/listar";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("persona", new Persona());
        return "personas/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam(defaultValue = "0") int id, Persona p, Model model) {
        var errores = service.validar(p);
        if (!errores.isEmpty()) {
            model.addAttribute("persona", p);
            model.addAttribute("errores", errores);
            return "personas/formulario";
        }
        try {
            if (id > 0) { p.setId(id); service.actualizar(p); }
            else service.guardar(p);
        } catch (Exception e) {
            log.error("Error al guardar persona: id={}", id, e);
            model.addAttribute("error", "Ocurrió un error al procesar la solicitud");
            model.addAttribute("persona", p);
            return "personas/formulario";
        }
        model.addAttribute("personas", service.listar());
        return "personas/listar";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable int id, Model model) {
        model.addAttribute("persona", service.obtenerPorId(id));
        return "personas/formulario";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable int id, Model model) {
        try {
            service.eliminar(id);
        } catch (DataIntegrityViolationException e) {
            log.error("Error al eliminar persona: id={}, violación de integridad", id, e);
            model.addAttribute("error", "No se puede eliminar: la persona tiene registros asociados (bautizos, confirmaciones o matrimonios).");
        } catch (Exception e) {
            log.error("Error al eliminar persona: id={}", id, e);
            model.addAttribute("error", "Ocurrió un error al procesar la solicitud");
        }
        model.addAttribute("personas", service.listar());
        return "personas/listar";
    }
}
