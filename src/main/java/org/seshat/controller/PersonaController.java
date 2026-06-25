package org.seshat.controller;

import org.seshat.model.Persona;
import org.seshat.service.PersonaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/personas")
public class PersonaController {
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
        if (id > 0) { p.setId(id); service.actualizar(p); }
        else service.guardar(p);
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
        service.eliminar(id);
        model.addAttribute("personas", service.listar());
        return "personas/listar";
    }
}
