package org.seshat.controller;

import org.seshat.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    public String dashboard(@RequestParam(required = false) Integer anio,
                            @RequestParam(required = false) Integer mes,
                            Model model) {
        model.addAttribute("stats", service.obtenerStats(anio, mes));
        model.addAttribute("anio", anio);
        model.addAttribute("mes", mes);
        model.addAttribute("anios", service.obtenerAniosDisponibles());
        return "dashboard/index";
    }
}
