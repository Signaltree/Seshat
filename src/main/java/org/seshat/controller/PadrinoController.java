package org.seshat.controller;

import org.seshat.model.Padrino;
import org.seshat.service.PadrinoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/padrinos")
public class PadrinoController {
    private static final Logger log = LoggerFactory.getLogger(PadrinoController.class);
    private final PadrinoService padrinoService;

    public PadrinoController(PadrinoService padrinoService) {
        this.padrinoService = padrinoService;
    }

    @GetMapping("/fragmento/{tipo}/{id}")
    public String fragmento(@PathVariable String tipo, @PathVariable int id, Model model) {
        List<Padrino> padrinos = padrinoService.listarPorSacramento(tipo, id);
        Map<Integer, String> roles = new HashMap<>();
        for (Padrino p : padrinos) {
            roles.put(p.getId(), padrinoService.obtenerRol(tipo, p.getId(), id));
        }
        model.addAttribute("padrinos", padrinos);
        model.addAttribute("roles", roles);
        model.addAttribute("tipo", tipo);
        model.addAttribute("entidadId", id);
        return "fragmentos/padrinos :: padrinos";
    }

    @PostMapping("/agregar")
    public String agregar(@RequestParam String tipo, @RequestParam int entidadId,
                          @RequestParam String nombres, @RequestParam String apellidos,
                          @RequestParam(required = false) String rut,
                          @RequestParam String rol,
                          @RequestParam(required = false) String rolOtro,
                          Model model) {
        try {
            String rolFinal = "Otro".equals(rol) && rolOtro != null && !rolOtro.isBlank() ? rolOtro.trim() : rol;
            Map<String, String> errores = padrinoService.agregar(tipo, entidadId, nombres, apellidos, rut, rolFinal);
            model.addAttribute("erroresPadrino", errores);
        } catch (Exception e) {
            log.error("Error al agregar padrino: tipo={}, entidadId={}", tipo, entidadId, e);
            model.addAttribute("error", "Ocurrió un error al procesar la solicitud");
        }

        List<Padrino> padrinos = padrinoService.listarPorSacramento(tipo, entidadId);
        Map<Integer, String> roles = new HashMap<>();
        for (Padrino p : padrinos) {
            roles.put(p.getId(), padrinoService.obtenerRol(tipo, p.getId(), entidadId));
        }
        model.addAttribute("padrinos", padrinos);
        model.addAttribute("roles", roles);
        model.addAttribute("tipo", tipo);
        model.addAttribute("entidadId", entidadId);
        return "fragmentos/padrinos :: padrinos";
    }

    @DeleteMapping("/{id}/{tipo}/{sacramentoId}")
    public String eliminar(@PathVariable int id, @PathVariable String tipo, @PathVariable int sacramentoId, Model model) {
        try {
            padrinoService.eliminar(tipo, id, sacramentoId);
        } catch (Exception e) {
            log.error("Error al eliminar padrino: id={}, tipo={}, sacramentoId={}", id, tipo, sacramentoId, e);
            model.addAttribute("error", "Ocurrió un error al procesar la solicitud");
        }

        List<Padrino> padrinos = padrinoService.listarPorSacramento(tipo, sacramentoId);
        Map<Integer, String> roles = new HashMap<>();
        for (Padrino p : padrinos) {
            roles.put(p.getId(), padrinoService.obtenerRol(tipo, p.getId(), sacramentoId));
        }
        model.addAttribute("padrinos", padrinos);
        model.addAttribute("roles", roles);
        model.addAttribute("tipo", tipo);
        model.addAttribute("entidadId", sacramentoId);
        return "fragmentos/padrinos :: padrinos";
    }
}
