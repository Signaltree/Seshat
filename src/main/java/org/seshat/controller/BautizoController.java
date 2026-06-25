package org.seshat.controller;

import org.seshat.model.Bautizo;
import org.seshat.service.BautizoService;
import org.seshat.service.PersonaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/bautizos")
public class BautizoController {
    private final BautizoService service;
    private final PersonaService personaService;

    public BautizoController(BautizoService service, PersonaService personaService) {
        this.service = service;
        this.personaService = personaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("bautizos", service.listar());
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
        if (id > 0) { b.setId(id); service.actualizar(b); }
        else service.guardar(b);
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
        service.eliminar(id);
        model.addAttribute("bautizos", service.listar());
        return "bautizos/listar";
    }
}
