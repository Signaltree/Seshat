package org.seshat.controller;

import org.seshat.model.Matrimonio;
import org.seshat.service.MatrimonioService;
import org.seshat.service.PersonaService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/matrimonios")
public class MatrimonioController {
    private final MatrimonioService service;
    private final PersonaService personaService;

    public MatrimonioController(MatrimonioService service, PersonaService personaService) {
        this.service = service;
        this.personaService = personaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("matrimonios", service.listar());
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
        if (id > 0) { m.setId(id); service.actualizar(m); }
        else service.guardar(m);
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
            model.addAttribute("error", "No se puede eliminar: el matrimonio tiene registros asociados.");
        }
        model.addAttribute("matrimonios", service.listar());
        return "matrimonios/listar";
    }
}
