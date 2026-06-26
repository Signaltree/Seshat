package org.seshat.service;

import org.seshat.model.Padrino;
import org.seshat.repository.PadrinoRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.seshat.util.ValidacionUtil;

@Service
public class PadrinoService {
    private final PadrinoRepository repo;

    public PadrinoService(PadrinoRepository repo) {
        this.repo = repo;
    }

    public List<Padrino> listarPorSacramento(String tipo, int sacramentoId) {
        return switch (tipo) {
            case "BAUTIZO" -> repo.findByBautizoId(sacramentoId);
            case "CONFIRMACION" -> repo.findByConfirmacionId(sacramentoId);
            case "MATRIMONIO" -> repo.findByMatrimonioId(sacramentoId);
            default -> throw new IllegalArgumentException("Tipo inválido: " + tipo);
        };
    }

    public Map<String, String> agregar(String tipo, int entidadId, String nombres, String apellidos, String rut, String rol) {
        Map<String, String> errores = new HashMap<>();
        if (nombres == null || nombres.isBlank()) errores.put("nombres", "El nombre es obligatorio");
        if (apellidos == null || apellidos.isBlank()) errores.put("apellidos", "Los apellidos son obligatorios");
        if (rol == null || rol.isBlank()) errores.put("rol", "El rol es obligatorio");
        if (rut != null) rut = rut.trim();
        if (rut != null && !rut.isBlank() && !ValidacionUtil.validarRut(rut)) {
            errores.put("rut", "RUT inválido");
        }
        if (!errores.isEmpty()) return errores;

        Padrino p = new Padrino(0, nombres.trim(), apellidos.trim(), rut);
        int padrinoId = repo.save(p);

        switch (tipo) {
            case "BAUTIZO" -> repo.insertarBautizoPadrino(entidadId, padrinoId, rol.trim());
            case "CONFIRMACION" -> repo.insertarConfirmacionPadrino(entidadId, padrinoId, rol.trim());
            case "MATRIMONIO" -> repo.insertarMatrimonioPadrino(entidadId, padrinoId, rol.trim());
        }
        return errores;
    }

    public void eliminar(String tipo, int padrinoId, int sacramentoId) {
        switch (tipo) {
            case "BAUTIZO" -> repo.eliminarBautizoPadrino(padrinoId, sacramentoId);
            case "CONFIRMACION" -> repo.eliminarConfirmacionPadrino(padrinoId, sacramentoId);
            case "MATRIMONIO" -> repo.eliminarMatrimonioPadrino(padrinoId, sacramentoId);
        }
        if (!repo.padrinoEstaReferenciado(padrinoId)) {
            repo.delete(padrinoId);
        }
    }

    public String obtenerRol(String tipo, int padrinoId, int sacramentoId) {
        return switch (tipo) {
            case "BAUTIZO" -> repo.obtenerRolBautizo(padrinoId, sacramentoId);
            case "CONFIRMACION" -> repo.obtenerRolConfirmacion(padrinoId, sacramentoId);
            case "MATRIMONIO" -> repo.obtenerRolMatrimonio(padrinoId, sacramentoId);
            default -> throw new IllegalArgumentException("Tipo inválido: " + tipo);
        };
    }
}
