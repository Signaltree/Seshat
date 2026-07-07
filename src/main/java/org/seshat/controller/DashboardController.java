package org.seshat.controller;

import org.seshat.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);
    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    public String dashboard(@RequestParam(required = false) Integer anio,
                            @RequestParam(required = false) Integer mes,
                            Model model) {
        try {
            model.addAttribute("stats", service.obtenerStats(anio, mes));
        } catch (Exception e) {
            log.error("Error al cargar dashboard: anio={}, mes={}", anio, mes, e);
            model.addAttribute("error", "Ocurrió un error al procesar la solicitud");
        }
        model.addAttribute("anio", anio);
        model.addAttribute("mes", mes);
        model.addAttribute("anios", service.obtenerAniosDisponibles());
        return "dashboard/index";
    }
}
