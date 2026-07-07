package org.seshat.controller;

import org.seshat.model.Confirmacion;
import org.seshat.service.ConfirmacionService;
import org.seshat.service.PersonaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/confirmaciones")
public class ConfirmacionController {
    private static final Logger log = LoggerFactory.getLogger(ConfirmacionController.class);
    private final ConfirmacionService service;
    private final PersonaService personaService;

    public ConfirmacionController(ConfirmacionService service, PersonaService personaService) {
        this.service = service;
        this.personaService = personaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("confirmaciones", service.listar());
        return "confirmaciones/listar";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("confirmacion", new Confirmacion());
        model.addAttribute("personas", personaService.listar());
        return "confirmaciones/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam(defaultValue = "0") int id, Confirmacion c, Model model) {
        try {
            if (id > 0) { c.setId(id); service.actualizar(c); }
            else service.guardar(c);
        } catch (Exception e) {
            log.error("Error al guardar confirmación: id={}", id, e);
            model.addAttribute("error", "Ocurrió un error al procesar la solicitud");
            model.addAttribute("confirmacion", c);
            model.addAttribute("personas", personaService.listar());
            return "confirmaciones/formulario";
        }
        model.addAttribute("confirmaciones", service.listar());
        return "confirmaciones/listar";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable int id, Model model) {
        model.addAttribute("confirmacion", service.obtenerPorId(id));
        model.addAttribute("personas", personaService.listar());
        return "confirmaciones/formulario";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable int id, Model model) {
        try {
            service.eliminar(id);
        } catch (DataIntegrityViolationException e) {
            log.error("Error al eliminar confirmación: id={}, violación de integridad", id, e);
            model.addAttribute("error", "No se puede eliminar: la confirmación tiene registros asociados.");
        } catch (Exception e) {
            log.error("Error al eliminar confirmación: id={}", id, e);
            model.addAttribute("error", "Ocurrió un error al procesar la solicitud");
        }
        model.addAttribute("confirmaciones", service.listar());
        return "confirmaciones/listar";
    }
}
