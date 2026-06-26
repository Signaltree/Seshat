# Padrinos Inline en Sacramentos

Feature: Agregar padrinos directamente en los formularios de bautizo, confirmación y matrimonio, sin CRUD separado.

## Requisitos

- Padrinos aislados por sacramento (no se reutilizan entre tipos)
- Cantidad variable por sacramento (agregar/eliminar dinámicamente)
- Cada padrino tiene: nombres, apellidos, RUT, rol
- El rol es un dropdown contextual según tipo de sacramento + opción "Otro"
- Mismo patrón HTMX que fotos-section y certificados-section

## Arquitectura

```
PadrinoController (nuevo)
├── GET /padrinos/fragmento/{tipo}/{sacramentoId}
│   → fragmento HTML con lista de padrinos + form agregar
├── POST /padrinos/agregar
│   → crea PADRINO + inserta en join table
│   → devuelve fragmento actualizado
└── DELETE /padrinos/{id}/{tipo}/{sacramentoId}
    → elimina join record + PADRINO si huérfano
    → devuelve fragmento actualizado

PadrinoService (nuevo)
├── listarPorSacramento(tipo, sacramentoId)
├── agregar(tipo, sacramentoId, nombres, apellidos, rut, rol)
└── eliminar(padrinoId, tipo, sacramentoId)

PadrinoRepository (extender)
├── findById(id)
├── save(nombres, apellidos, rut) → int id
├── delete(id)
├── insertarBautizoPadrino(bautizoId, padrinoId, rol)
├── insertarConfirmacionPadrino(...)
├── insertarMatrimonioPadrino(...)
├── eliminarBautizoPadrino(padrinoId, bautizoId)
├── eliminarConfirmacionPadrino(...)
├── eliminarMatrimonioPadrino(...)
├── findByBautizoId(bautizoId)
├── findByConfirmacionId(confirmacionId)
└── findByMatrimonioId(matrimonioId)
```

## Roles por tipo de sacramento

| Sacramento | Opciones dropdown |
|---|---|
| BAUTIZO | Padrino, Madrina |
| CONFIRMACION | Padrino, Madrina |
| MATRIMONIO | Testigo |
| (todos) | Otro... |

Si selecciona "Otro", aparece un input text para escribir el rol libremente.

## Endpoints HTMX

### GET /padrinos/fragmento/{tipo}/{sacramentoId}

Usado como `hx-get` con `hx-trigger="load"` en formularios de sacramento.

Respuesta: fragmento HTML con:
- Lista de padrinos (nombre, apellido, RUT, rol) con botón eliminar cada uno
- Formulario inline para agregar nuevo padrino (nombres, apellidos, RUT, dropdown rol + campo "Otro")
- Estado vacío: "Sin padrinos registrados"

### POST /padrinos/agregar

Parámetros: `tipo`, `entidadId`, `nombres`, `apellidos`, `rut`, `rol`, `rolOtro`

- Si `rol == "Otro"`, usa `rolOtro` como rol
- Valida RUT con ValidacionUtil
- Crea PADRINO (save)
- Inserta join table según tipo
- Devuelve fragmento actualizado con el nuevo padrino en la lista

### DELETE /padrinos/{id}/{tipo}/{sacramentoId}

- Elimina join record según tipo
- Elimina PADRINO (si ya no está referenciado en ninguna join table)
- Devuelve fragmento actualizado

## Cambios en sacramentos existentes

### BautizoService.eliminar()
```java
public void eliminar(int id) {
    padrinoRepository.eliminarBautizoPadrinosPorBautizo(id);
    repo.delete(id);
}
```

### ConfirmacionService.eliminar()
```java
public void eliminar(int id) {
    padrinoRepository.eliminarConfirmacionPadrinosPorConfirmacion(id);
    repo.delete(id);
}
```

### MatrimonioService.eliminar()
```java
public void eliminar(int id) {
    padrinoRepository.eliminarMatrimonioPadrinosPorMatrimonio(id);
    repo.delete(id);
}
```

### Formularios HTML

Cada formulario de sacramento recibe el siguiente bloque al final (antes de la sección de certificados):

```html
<div th:if="${bautizo.id != 0}"
     th:attr="hx-get=@{/padrinos/fragmento/BAUTIZO/{id}(id=${bautizo.id})}"
     hx-trigger="load"
     hx-target="this"
     hx-swap="outerHTML">
</div>
```

## Template

`src/main/resources/templates/fragmentos/padrinos.html`

Mismo diseño visual que `fotos-section`: contenedor con borde, grid de padrinos, cada uno con nombre + rol + botón eliminar, y form agregar debajo.

## Validación

- RUT validado con ValidacionUtil (mismo que personas)
- Nombres y apellidos requeridos
- Rol requerido

## Eliminación de padrino huérfano

Al eliminar un padrino de un sacramento, se revisa si el PADRINO está referenciado en las otras join tables. Si no lo está, se elimina de PADRINO. Si aún está referenciado (caso raro pero posible con datos preexistentes), solo se elimina la join record.
