# API.md — estado del proyecto

Bitácora y estado vivo del backend: qué está hecho, qué sigue, pendientes, datos de prueba, hallazgos, requisitos por módulo e historia. **Las reglas de cómo trabajamos y las convenciones están en `CLAUDE.md`**; aquí va lo que cambia con cada sesión.

Al cerrar un bloque se actualizan: "Retomar aquí", "Pendientes", "Estado de los módulos" y la "Bitácora".

**Última verificación contra el código:** 2026-10-31 (acción `PINTAR` en Valoración, `feature/valoracion-accion`). Antes: 2026-10-27 (historial de etapas en `feature/historial-etapas`) y 2026-10-26 (Valoración completa; esquema de la compartida comparado contra el modelo). La revisión completa anterior es del 2026-10-25.

---

## 1. Retomar aquí (actualizado 2026-10-25)

**Repo:** `develop` = `1368240` (PR #18, Valoración crear/obtener). Árbol limpio. `main` va muy atrás de `develop`. `./mvnw test` pasa (solo existe `contextLoads`).

**Base en uso:** la **local** (XAMPP, MariaDB 10.4) cuando la red bloquea Tailscale. Ver sección 5.

**Ya está en `develop` (antes figuraba como pendiente):** Valoración `POST`/`GET`; Usuario con `/desactivados` y `/reactivar` (`a4f7095`); CORS centralizado en `SecurityConfig` leyendo `CORS_ALLOWED_ORIGINS` del `.env` (`724789a`); columna "En curso" de técnicos (`enCurso` en `TecnicoResponseDto`, `25ebac7`); el mensaje de `validarActiva` ya dice "solo se puede modificar una orden activa".

### Revisión completa del 2026-10-25 — orden sugerido
Antes de conectar el front, en este orden (detalle de cada punto en la sección 2):
1. **Corregir los errores E1–E7** (sección 2.0): cancelar una orden cerrada, etapa abierta al cancelar, cambio de vehículo, filtro JWT en el login, `@CrossOrigin` sobrante, enumeración de usuarios inactivos, último ADMIN.
2. **Acordar el contrato con el front** (sección 2.1): las rutas del front (`/ordenes`, `POST`, etc.) no coinciden con las del backend, y la "vista de orden" del front pide campos que el backend no calcula.
3. **Endpoints que faltan para las pantallas** (sección 2.2): etapas/historial de la orden, campos de Kanban y ficha, dashboard, terminar Valoración, nueva orden compuesta, `/api/auth/me`, control por rol.
4. Mejoras no bloqueantes (sección 2.3).

### Próxima sesión (dejado el 2026-10-27)
- **Historial de etapas hecho** en `feature/historial-etapas`, sin commit: `GET /api/orden/{idOrden}/historial` y campo `asesor` en `OrdenReparacionResponseDto`, probados con curl en la compartida (lecturas + cambio de etapa en vivo con datos de prueba ya borrados). Falta: commit y PR a `develop`. Pendiente de decisión: quitar la inversa `OrdenReparacion.historialEtapas` (nadie la usa).
- Valoración editar/CESVI/listados/conteo ya está en `develop` (`39fa877`).
- Siguiente bloque: **errores E1–E4** (sección 2.0).
- **Edición = reemplazo completo** (decidido): el front manda **la hoja entera**; si manda solo las piezas nuevas, las demás se pierden. Las piezas que no cambian se reinsertan con id nuevo (hoy nada apunta a `valoracion_detalle`). Si algún día hace falta un id estable (p. ej. un costo por pieza), pasar a "por diferencias": `idValoracionDetalle` opcional en el DTO, validando que pertenezca a esa valoración. Caso extra de prueba: 5 piezas A–E → PUT con A, B, F, G, H → quedan 5.
- **Propuesta de Python (sin confirmar):** las alertas del Dashboard van en Java (son consultas simples, deben ser instantáneas y reutilizan `convertirADto` y la seguridad). Los **reportes** (entregas por mes, tiempos por etapa, carga por técnico, Excel) en una carpeta `reportes/` dentro de este repo, ejecutados por Spring con `ProcessBuilder` (el front ve una sola API y un solo JWT). Requiere Python en el servidor y un usuario MySQL de solo lectura. Falta que el taller defina qué reportes quiere.
- **Hallazgo en el mock del front:** `calcularAlertas` cuenta "Fuera del taller" sin filtrar por estado, así que incluiría las órdenes entregadas. El backend debe contar solo las `ACTIVA`. "Movimientos recientes" no registra el marcado de fechas (es `LocalDate` sin hora): si se quiere, hace falta una tabla de eventos.

### Valoración — lo que falta
- Fotos, vista PDF y ZIP (bloqueado por el almacenamiento de fotos).
- `ValoracionRepository.findByCargadaCesvi` quedó sin uso (los listados filtran desde `OrdenReparacionRepository`).
- **Acción `PINTAR` (2026-10-31, `feature/valoracion-accion`, sin commit):** hecha y probada en la base local. **Falta en la compartida** rehacer el CHECK de `valoracion_detalle.accion` (hoy solo acepta `REPARACION`/`SUSTITUCION`: guardar `PINTAR` daría el 409 genérico) con `ALTER TABLE valoracion_detalle MODIFY accion VARCHAR(20) NOT NULL CHECK (accion IN ('REPARACION','SUSTITUCION','PINTAR'));` (o sin el CHECK), revisando antes con `SHOW CREATE TABLE` y avisando al equipo.
- **Front (hoja de valoración):** falta la sección "III · PINTURA" (con gravedad, igual que Reparación). El mock muestra la gravedad como `LEVE / MEDIA / GRAVE` y el enum es `LEVE / MEDIO / FUERTE`: decidir si el front traduce las etiquetas (recomendado) o se renombra el enum (obliga a migrar datos y rehacer el CHECK de `gravedad`).

**Decisiones en Valoración:** costos (`costo`, `costoEstimado`) fuera de los DTOs; `gravedad` obligatoria con REPARACION y PINTAR y prohibida con SUSTITUCION (409); piezas ordenadas por el orden de declaración de `Accion` (SUSTITUCION → REPARACION → PINTAR) y alfabéticas dentro de cada grupo; `cantidad` en todas las acciones; crear/editar solo en `ACTIVA`; piezas sin `DELETE` propio (se eliminan reemplazando la lista en el `PUT`). **CESVI** se puede marcar en `ACTIVA` y `CERRADA` (la carga suele ser después de entregar), no en `CANCELADA`; marcar dos veces → 409 (no se pisa `cargadaCesviAt`). **Filtros:** `SIN_VALORAR` = `ACTIVA` sin valoración; `SIN_CARGAR` / `CARGADAS` = con valoración y `cargadaCesvi` false/true, estado ≠ `CANCELADA`. Cada orden cae en un solo filtro; las canceladas en ninguno. El conteo usa los mismos criterios que las listas.

### Planeado: almacenamiento de fotos (propuesta 2026-10-24, sin confirmar)
Disco local del servidor de Adrian (fuera de carpetas públicas, ruta en `FOTOS_DIR`), archivos con nombre UUID, en la base la ruta relativa; interfaz `AlmacenamientoService` para poder pasar a MinIO/S3; al subir: límite de tamaño, verificar tipo real, re-codificar, quitar EXIF (GPS), redimensionar y miniatura; acceso por **URLs firmadas HMAC de corta duración** (`/api/archivos/...?exp=&firma=`), que también sirven para WhatsApp; exponer el servidor con Cloudflare Tunnel; respaldo diario a disco externo + copia fuera de casa; UPS. **Pendiente que Adrian confirme:** el enfoque, cómo expone el servidor (¿dominio?), SO/disco del servidor y el volumen de fotos (estimado ~1,2 GB/mes con 30 fotos × 100 órdenes).

### Lo que hace Williams
Control por rol (`hasAnyRole` en `SecurityConfig`) — ver 2.2.

---

## 2. Pendientes vigentes

### 2.0 Errores encontrados en la revisión del 2026-10-25 (corregir antes del front)
- [ ] **E1 — Se puede cancelar una orden `CERRADA`.** `OrdenReparacionService.deleteOrdenById` no valida el estado; luego `reactivar` la deja `ACTIVA` con `fechaDeEntregaReal` y `FUERA_DE_TALLER`. Permitir cancelar solo `ACTIVA` (409 en otro caso).
- [ ] **E2 — Cancelar deja la `OrdenEtapaFecha` abierta**: el técnico sigue contando esa orden en "En curso". Cerrarla al cancelar (y decidir qué pasa al reactivar).
- [ ] **E3 — `updateOrden` permite cambiar el vehículo** de una orden con etapas, valoración y observaciones (el historial queda en otro carro). Propuesta: no cambiar el vehículo después de `fechaIngresoReparacion` (o nunca); solo corregir el cliente.
- [ ] **E4 — El filtro JWT corre en `/api/auth/login`**: con un token vencido en la cabecera (el interceptor de axios del front siempre lo manda) el login responde 401 "El token ha expirado". Agregar `shouldNotFilter` para `RUTAS_PUBLICAS`.
- [x] **E5 (corregido 2026-10-27, `feature/historial-etapas`) — `ObservacionController` conservaba `@CrossOrigin(origins = "*")`**, que se suma al CORS central y abre ese controlador a cualquier origen. Quitarlo.
- [ ] **E6 — `AuthService.login` revisa `activo` antes que la contraseña**: permite saber, sin la clave, que un email existe y está inactivo. Validar la contraseña primero.
- [ ] **E7 — Usuarios:** un ADMIN puede desactivarse a sí mismo o quitarse el rol, y se puede desactivar o degradar al último ADMIN. Todavía se acepta el rol `TECNICO` en alta/edición.
- [ ] **E8 —** `application.properties`: el comentario de `jwt.expiration=3600` dice "2 horas" (es 1 h). Decidir el valor y corregir el comentario.
- [ ] **E9 —** `UsuarioInactivoException` responde **403** (handler y filtro); CLAUDE.md dice 401. Unificar.
- [x] **E10 (2026-10-26, corregido el mismo día) —** un `@RequestParam` obligatorio ausente (`GET /api/valoracion/ordenes` sin `filtro`) responde **500**: falta el handler de `MissingServletRequestParameterException` en `GlobalExceptionHandler` (→ 400).

### 2.1 Contrato con el front (`grc-front`)
El front hoy corre con mocks (`REACT_APP_USE_MOCK`); solo Técnicos y Usuarios hablan con el backend real. Al apagar el mock, estas diferencias rompen:
- [ ] **Rutas distintas:** el front usa `/ordenes`, `/ordenes/{id}`, `/ordenes/buscar?placa=`, `/ordenes/kanban`, `/ordenes/{id}/historial`, `POST /ordenes/{id}/cambiar-etapa`, `POST /ordenes/{id}/ingreso-cotizar|ingreso-reparacion|dias-estimados`, `/vehiculos/{placa}`, `/clientes/{documento}`, `/ordenes/{id}/observaciones`, `/ordenes/{id}/fotos`, `PUT /ordenes/{id}/valoracion` (crear y editar), `POST .../valoracion/cesvi`, `/valoraciones/ordenes?filtro=`, `/valoraciones/conteo`, `/dashboard/resumen|avisos|movimientos|carga-etapas`. El backend usa `/api/orden`, `PATCH`, `/vehiculo/placa/{placa}` y `/cliente/documento/{documento}`. Decidir quién se adapta (recomendado: el front ajusta rutas y verbos a lo que ya existe; el backend agrega lo que falta).
- [ ] **Fechas sin cuerpo:** el front marca cotizar/ingreso sin enviar fecha; el backend exige `{ "fecha": "YYYY-MM-DD" }`.
- [ ] **Búsquedas:** el front espera `null` cuando no existe la placa o el documento; el backend responde 404. El front debe tratar el 404 como "no existe".
- [ ] **"Vista de orden" del front** (`grc-front/src/services/README.md`) pide campos que el backend no da: `tecnicoActual`, `fechaInicioEtapa`, `diasEnEtapa`, `diasEnTaller`, `etapasVisitadas`, `asesor`, `alertas` / `alerta`, `estadoValoracion`, `vehiculoTexto` y `siniestro` (este último no existe en el modelo). El front usa `estado = 'ENTREGADA'`; el backend usa `CERRADA`. El brief del front habla de "10 etapas"; son 9 (+ Asignado y Entregado calculadas).
- [ ] **Valoración (2026-10-26):** el front debe usar `PUT /api/orden/{id}/valoracion` para editar (crear sigue siendo `POST`), `PATCH /api/orden/{id}/valoracion/cesvi` (no `POST`), `GET /api/valoracion/ordenes?filtro=SIN_VALORAR|SIN_CARGAR|CARGADAS` (no los textos 'Sin valorar'…) y `GET /api/valoracion/conteo`, que devuelve `{ sinValorar, sinCargar, cargadas }` en vez de claves con el texto del filtro. El mock del front no filtra por estado; el backend excluye las canceladas.
- [ ] **Formato de errores:** el front lee `data.message || data.error`. Los 400 de validación devuelven `{ campo: mensaje }` sin `error`, así que el usuario vería "Request failed with status code 400". Unificar (p. ej. `{ "error": "...", "campos": { ... } }`).
- [ ] El interceptor del front borra la sesión ante cualquier 401; con E4 corregido, un login fallido no afecta.

### 2.2 Endpoints que faltan para las pantallas
- [x] **Historial de la orden** (2026-10-27): `GET /api/orden/{idOrden}/historial` desde `HistorialEtapas`.
- [x] **Etapas de la orden** (2026-10-31, `feature/etapas-orden`, sin probar contra la base): `GET /api/orden/{idOrden}/etapas` desde `OrdenEtapaFecha`, con técnico y `fechaFin`. Lo usan las fichas verde/naranja/gris y el Kanban del front.
- [ ] **Campos calculados en la orden:** técnico actual, "desde", días en etapa, días en taller, alertas. (`asesor` hecho el 2026-10-27.)
- [ ] **Dashboard** completo: 6 tarjetas, listas por aviso, "+X ingresos hoy", movimientos de las últimas 24 h, carga por etapa.
- [ ] **Nueva orden compuesta** en un solo `@Transactional` (las 4 combinaciones placa/documento). Hoy serían 3 llamadas (cliente → vehículo → orden) y una falla deja registros sueltos.
- [ ] Placa repetida (también de un vehículo desactivado): mensaje claro / ofrecer reactivar (hoy 409 genérico).
- [ ] Órdenes: ¿dos órdenes `ACTIVA` para el mismo vehículo? ¿validar que el vehículo y el cliente estén activos al crear?
- [ ] `GET /api/auth/me` (recuperar el usuario al recargar).
- [ ] **Control por rol** según la tabla de CLAUDE.md (`hasAnyRole`). **Lo hace Williams.** Hoy cualquier autenticado entra a todo.
- [ ] Imágenes de Observaciones y de Valoración, PDF, ZIP y pestaña Fotografías (bloqueado por el almacenamiento de fotos).
- [ ] Notificaciones WhatsApp `@Async` + `PlantillaMensaje`; `Modulo`/`RolModulo`.
- [ ] Usuario semilla (`CommandLineRunner`) para cualquier base nueva.

### 2.3 Mejoras (no bloquean)
**Reglas de negocio**
- [ ] Las observaciones exigen una etapa visitada: una orden en cotización o en "Asignado" no puede recibir notas. Confirmar con el taller.
- [ ] `cambiarEtapa` no pone `ubicacionActual = EN_TALLER` (si una observación la dejó `FUERA_DE_TALLER`, la orden sigue "fuera" aunque avance; la observación del cambio sí dice `EN_TALLER`).
- [ ] Un técnico puede cambiar de especialidad mientras tiene una etapa abierta de la especialidad anterior.

**Datos y validación**
- [ ] `Vehiculo.anio` sin rango (`@Min`/`@Max`).
- [ ] `correo` de cliente/técnico sin `EmailUtil` (ni `trim` ni minúsculas), y un `""` se guarda vacío en vez de `NULL`.
- [ ] Vehículo: `color` y `vin` sin `trim`; `vin` sin pasar a mayúsculas. `Usuario.nombre` sin `trim`.
- [ ] `Vehiculo.modelo` en 10 se queda corto (`Sandero Stepway`).
- [ ] Declarar `UNIQUE(id_orden_reparacion, id_etapa)` en `OrdenEtapaFecha` (`@Table(uniqueConstraints = ...)`) — hoy solo existe en la base (en la compartida se había perdido y se repuso como `uk_orden_etapa` el 2026-10-26).
- [ ] `length` en `Notificaciones` (`evento_disparador`, `canal`, `contenido_enviado`, `error_detalle`); evaluar `TEXT` para `contenido_enviado` / `contenido_template`.
- [ ] Igualar `ImagenObservacion.url` (255) con `ValoracionImagen.url` (2083).
- [ ] `Modulo.codigo` en 10 queda justo para códigos como `DETALLE_OT`. Tablas `modulo` y `plantilla_mensaje` vacías.

**Rendimiento**
- [ ] `FetchType.LAZY` en las relaciones (hoy 0); `OrdenReparacion.valoracion` es un `@OneToOne` inverso que se carga siempre, con una consulta extra por orden.
- [ ] `TecnicoService.convertirADto` hace un `count` por técnico; `OrdenReparacionService.convertirADto` hace una consulta por orden para el `asesor` (afecta todas las listas de órdenes).
- [ ] Paginación (`/entregadas` crece sin fin).

**Operación / producción**
- [ ] Flyway en vez de `ddl-auto=update` (urgente con la base compartida); `show-sql=false`; `open-in-view=false`; HTTPS con proxy; respaldos.
- [ ] Quitar `mysql-connector-j` del `pom.xml` (ojo: el `application.properties` de test usa `MySQLDialect`).
- [ ] Tests reales: solo existe `contextLoads`. Pasar los casos de curl a `MockMvc` o a una colección de Postman.
- [ ] Llevar `develop` a `main`.

**Estilo**
- [ ] Unificar Lombok vs. accesores a mano (decisión abierta).
- [ ] **Quitar `OrdenReparacion.historialEtapas`** (`@OneToMany` inverso): desde 2026-10-27 el historial y el `asesor` se leen por `HistorialEtapasRepository` y nadie llama a `getHistorialEtapas()`. CLAUDE.md: inversas solo si se navegan. Sin cambio en la base (el lado inverso no tiene columna). `OrdenReparacion.ordenEtapaFechas` está igual (nadie llama a `getOrdenEtapaFechas()`): decidir las dos juntas.
- [ ] `UsuarioService` usa `Collectors.toList()` (los demás `.toList()`); `ValoracionImagen` tiene el tipo totalmente calificado en el constructor; sobra el `@JsonIgnore` de `Cliente.vehiculos`; mover `LoginRequest`/`LoginResponse` a `dto/auth`; formato de los `record` de `dto/etapas`.

### Módulos de negocio terminados
- [x] `Etapas` (catálogo, 2026-10-20).
- [x] Cambio de etapa (2026-10-22, PR #9).
- [x] Fechas del ciclo de vida (2026-10-24).
- [x] Entrega `PATCH /api/orden/{id}/entregar-orden` y `GET /api/orden/entregadas` (2026-10-24).
- [x] Observaciones (solo texto) (2026-10-24).
- [x] Valoración crear/obtener (2026-10-24, PR #18).
- [x] Valoración editar, CESVI, listados y conteo (2026-10-26, `39fa877`).
- [x] Historial de etapas + `asesor` en la orden (2026-10-27, `feature/historial-etapas`, sin commit).
- [x] Usuario: `/desactivados` y `/reactivar` (`a4f7095`).
- [x] CORS central desde `.env` (`724789a`).
- [x] Técnicos: columna "En curso" (`25ebac7`).
- **Decisión abierta:** almacenamiento de fotos — hay propuesta (sección 1), falta que Adrian la confirme.

---

## 3. Estado de los módulos (API)

Todos los DTOs son `record`; ningún controlador devuelve una entidad.

| Módulo | Ruta | Endpoints |
|---|---|---|
| `Auth` | `/api/auth` | `POST /login` → `{"token": "...", "usuario": UsuarioResponseDto}` (desde `c3706c5`, del compañero) |
| `Usuario` | `/api/usuario` | GET lista (`findByActivoTrue`), GET `/desactivados`, GET `/{id}`, POST (201), PUT `/{id}`, DELETE (204, lógico), PATCH `/{id}/reactivar` |
| `Cliente` | `/api/cliente` | POST, PUT `/{id}`, GET `/{id}`, GET `/documento/{documento}`, GET lista, GET `/desactivados`, DELETE (lógico), PATCH `/{id}/reactivar` |
| `Tecnico` | `/api/tecnico` | GET lista, GET `/desactivados`, GET `/{id}`, GET `/especialidad/{especialidad}` (activos; valor inválido o en minúsculas → 400), POST, PUT `/{id}`, DELETE (lógico), PATCH `/{id}/reactivar` |
| `Vehiculo` | `/api/vehiculo` | GET `/{id}`, GET `/placa/{placa}`, GET lista, GET `/desactivados`, POST, DELETE (lógico), PATCH `/{id}/reactivar`. Sin update (a propósito) |
| `OrdenReparacion` | `/api/orden` | POST, PUT `/{id}` (solo `ACTIVA`), DELETE `/{id}` (→ `CANCELADA`), PATCH `/{id}/reactivar` (solo desde `CANCELADA`), GET `/{id}`, GET (activas), GET `/entregadas` (CERRADA, la más reciente primero), GET `/placa/{placa}`, PATCH `/{id}/etapa`, PATCH `/{id}/ingreso-cotizar`, PATCH `/{id}/ingreso-reparacion`, PATCH `/{id}/dias-estimados`, PATCH `/{id}/entregar-orden` |
| `Etapas` | `/api/etapa` | GET lista (por `orden`), GET `/{id}`, POST, PUT `/{id}`. Sin DELETE, a propósito |
| `Observacion` | `/api/orden/{idOrden}/observaciones` | POST (201), GET (la más reciente primero). `ObservacionesService` + `ObservacionController` |
| `Valoracion` | `/api/orden/{idOrden}/valoracion` y `/api/valoracion` | POST (201), GET, PUT (200, reemplaza la hoja completa), PATCH `/cesvi` (200; 409 si ya está marcada o la orden está cancelada); `GET /api/valoracion/ordenes?filtro=SIN_VALORAR\|SIN_CARGAR\|CARGADAS` (lista de `OrdenReparacionResponseDto`), `GET /api/valoracion/conteo`. Enum `FiltroValoracion`, `ConteoValoracionResponseDto`; `OrdenReparacionService.convertirADto` es público |
| `HistorialEtapas` | `/api/orden/{idOrden}/historial` | GET (cronológico, el más antiguo primero; `[]` si nunca cambió de etapa; 404 si la orden no existe). Solo lectura: las filas las escribe `cambiarEtapa`. `HistorialEtapasService` + `HistorialEtapasController`, `HistorialEtapasResponseDto(idHistorialEtapas, idOrdenReparacion, idEtapa, nombreEtapa, usuario, fechaCambio)` |

| `OrdenEtapaFecha` | `/api/orden/{idOrden}/etapas` | GET (por `fechaInicio`, la más antigua primero; `[]` si nunca entró a una etapa; 404 si la orden no existe). Solo lectura: las filas las escribe `cambiarEtapa` (y la entrega cierra la abierta). `OrdenEtapaFechaService` + `OrdenEtapaFechaController`, `OrdenEtapaFechaResponseDto(idOrdenEtapaFecha, idOrdenReparacion, idEtapa, nombreEtapa, idTecnico, nombreTecnico, fechaInicio, fechaFin, completada)`; `idTecnico`/`nombreTecnico` en `null` en etapas sin técnico |

Sin endpoints todavía: Dashboard, `/api/auth/me`, fotos, notificaciones.

Infraestructura:
- `config/SecurityConfig` + `config/JwtAuthenticationFilter` + `config/SecurityErrorWriter`, `service/JwtService`, `service/CustomUserDetailsService`.
- `exception/`: `RecursoNoEncontradoException`, `ReglaNegocioException`, `EmailAlreadyExistsException`, `CredencialesInvalidasException`, `UsuarioInactivoException`, `GlobalExceptionHandler` (probado contra el servidor).
- `util/`: `CelularUtil` (E.164), `DocumentoUtil` (quita espacios y puntos), `PlacaUtil` (quita espacios y guiones, mayúsculas), `EmailUtil` (`trim().toLowerCase()`).
- Usan `@Transactional`: `OrdenReparacionService`, `ValoracionService`, `ObservacionesService` y `HistorialEtapasService`. Los servicios de catálogo no lo usan.

### Seguridad
- **JWT funcionando.** `csrf` desactivado, sesión `STATELESS`, públicas solo `/api/auth/login` y `/error`; lo demás exige `Bearer` (sin token → 401 `"No autorizado. Debes iniciar sesión."`).
- Token: email como `subject`, `rol` como claim, expira en `jwt.expiration` (3600 s).
- `AuthService.login`: normaliza el email, valida `activo`, compara con BCrypt; email o contraseña incorrectos → mismo 401 genérico.
- **Falta control por rol** (cualquier autenticado entra a todo). `CustomUserDetailsService` ya carga el rol como authority.
- **CORS central** (`724789a`): bean `CorsConfigurationSource` en `SecurityConfig` con los orígenes de `CORS_ALLOWED_ORIGINS` (default `http://localhost:3000`). Solo queda `@CrossOrigin("*")` en `ObservacionController` (error E5).
- Errores de seguridad con forma `{"error": "..."}`: sin token 401, token vencido 401 "El token ha expirado", usuario inactivo 403.

### Cambio de etapa (`PATCH /api/orden/{id}/etapa`, 2026-10-22)
- Repositorios: `OrdenEtapaFechaRepository` con `existsByOrdenReparacionIdOrdenReparacionAndEtapaIdEtapa` y `findByOrdenReparacionIdOrdenReparacionAndFechaFinIsNull` (`Optional`); `HistorialEtapasRepository` con `findByOrdenReparacionIdOrdenReparacionOrderByFechaCambioAsc` (historial) y `findFirstBy...OrderByFechaCambioDesc` (`Optional`, asesor), desde 2026-10-27.
- `CambioEtapaRequestDto(idEtapaDestino @NotNull, idTecnico, nota @Size(max=500), visibleCliente)`.
- `OrdenReparacionService.cambiarEtapa`: bloque A de validaciones (orden 404, `ACTIVA` 409, `fechaIngresoReparacion` 409, etapa 404, ya visitada 409, técnico sobra/falta/inactivo/especialidad 409, inexistente 404) y bloque B de escrituras con un solo `ahora`. Observación solo si la nota no está en blanco, con `visibleCliente = Boolean.TRUE.equals(...)`.
- Probado con curl (18 casos) en la compartida: `fechaFin` de cada etapa = `fechaInicio` de la siguiente, una sola etapa abierta, nota con `trim`. Volver a una etapa saltada (Pintura → Bancada) → 200.
- `TecnicoService.buscarTecnico` y `EtapasService.buscarEtapa` son públicos.
- La notificación WhatsApp (`@Async`) queda para después.

### Detalles por módulo
- **Cliente:** `documento` acepta 6–10 dígitos con punto o espacio entre grupos (`1.017.171.717`); se guarda solo dígitos. El guion se rechaza (en un NIT separa el dígito de verificación). Duplicado → 409, solo entre clientes. `GET /documento/{documento}` devuelve también inactivos. `ClienteUpdateDto` no trae `documento`; es PUT completo (un `correo` omitido queda `NULL`).
- **Tecnico:** mismas reglas de documento; duplicado → 409 solo entre técnicos. `TecnicoUpdateDto` no trae `documento` ni `activo`. Formulario sin `correo` (queda `NULL`). La respuesta trae `enCurso` (`OrdenEtapaFecha` abiertas del técnico).
- **Vehiculo:** la placa acepta espacios alrededor y un espacio o guion (`abc 123`, `ABC-123`) → se guarda `ABC123`. Placa repetida → 409 genérico del UNIQUE (también si está desactivado).
- **Usuario:** `UsuarioRequestDto` (nombre 3–50, email, rol, password 8–60 con mayúscula, minúscula y número); `UsuarioUpdateDto` con `password` opcional (vacía = no cambiar). Nunca expone `passwordHash`. Todavía acepta el rol `TECNICO`.
- **OrdenReparacion:** `OrdenReparacionRequestDto(idVehiculo, idCliente)`; el cliente puede ser distinto al dueño del vehículo. `OrdenReparacionResponseDto` anida vehículo y cliente, `usuarioCreador` como nombre, `asesor` (nombre del último `HistorialEtapas.usuario`; `null` si nunca cambió de etapa), etapa como `idEtapaActual` + `nombreEtapaActual` (`null` = "Asignado") y todas las fechas. Se permiten dos órdenes `ACTIVA` por vehículo y no se valida que vehículo o cliente estén activos (pendiente).
- **Etapas:** nombre (ignorando mayúsculas y espacios) y orden únicos → 409 en alta y edición (`...AndIdEtapaNot`). Para intercambiar dos etapas hay que pasar por una posición libre.

---

## 4. Estado del modelo (entidades)

Verificado contra el código el 2026-10-20.

| Entidad | Estado | Notas |
|---|---|---|
| `Usuario` | ✅ | Lombok. `nombre` (50), `email` (100, UNIQUE), `passwordHash` (60, BCrypt), `rol`, `activo`. Sin relación con `Tecnico`. Sin `createAt`/`updateAt`. Con capas + Auth |
| `Modulo` | ✅ | `codigo` UNIQUE. Sin capas; tabla vacía |
| `RolModulo` | ✅ | `@ManyToOne` a `Modulo`, enum `rol`. Sin capas |
| `Persona` | ✅ | Lombok. Abstracta, `JOINED`. `documento` (10, sin UNIQUE, índice `idx_persona_documento`), `nombreCompleto` (200), `celular` (16), `correo` (100) nullable |
| `Cliente` | ✅ | Lombok. `activo`, `createAt`, `updateAt`, `@OneToMany vehiculos` (con `@JsonIgnore` innecesario). Con capas |
| `Tecnico` | ✅ | Lombok. `especialidad` (enum, `NOT NULL`), `activo`, `createAt`, `updateAt`. Con capas |
| `Vehiculo` | ✅ | Lombok. `placa` (6, UNIQUE `uk_vehiculo_placa` en la compartida), `marca` (20), `modelo` (10), `anio` `Short` nullable, `color`, `vin` nullable, `activo`, timestamps. Con capas (sin update) |
| `Etapas` | ✅ | Lombok. `nombre` (`nombre_etapa`, 100), `orden`, `descripcion` (255), `diasLimiteCritico` (`Short`), `especialidad` (nullable). En la base `especialidad` tiene un `CHECK` de los 6 valores. Con capas |
| `OrdenReparacion` | ✅ | Lombok (sin constructor completo). `etapaActual` y fechas nullable; `usuarioCreador` `NOT NULL`; `ubicacionActual` nullable. 4 `@ManyToOne` EAGER. Con capas + cambio de etapa |
| `HistorialEtapas` | ✅ | A mano. Log append-only: orden, etapa, usuario, `fechaCambio` |
| `OrdenEtapaFecha` | ✅ | A mano. `tecnico` nullable, `fechaInicio`, `fechaFin`, `completada`. **El UNIQUE(orden, etapa) no está declarado en la entidad** (ver Hallazgos) |
| `Valoracion` | ✅ | **Lombok desde 2026-10-24.** `@OneToOne` con `id_orden_reparacion` UNIQUE; `descripcionGeneral` (500); `usuario` `NOT NULL`; `costoEstimado` sin uso. Sin cascada. Capas en curso |
| `ValoracionDetalle` | ✅ | **Lombok desde 2026-10-24.** `cantidad` (`Short`, `NOT NULL`, default 1; columna agregada a mano). Regla de `gravedad` en el Service; `costo` sin uso. Sin Service propio (lo maneja `ValoracionService`) |
| `ValoracionImagen` | ✅ | `url` (2083), `descripcion` (300, sin uso funcional, se conserva). Sin capas |
| `Observacion` | ✅ | `nota` (500); única con cascada a `imagenes`. Con capas (`ObservacionesService`, `ObservacionController`) |
| `ImagenObservacion` | ✅ | PK `idImagenesObservacion`. Sin capas |
| `Notificaciones` | ✅ | `imagenesEnviadas` como `JSON`; 4 columnas sin `length`. Sin capas |
| `PlantillaMensaje` | ✅ | Sin FK (se consulta por `evento`). Sin capas; tabla vacía |

- **Estilo de accesores:** con Lombok → `Persona`, `Cliente`, `Tecnico`, `Vehiculo`, `Usuario`, `OrdenReparacion`, `Etapas`, `Valoracion` y `ValoracionDetalle` (estas dos pasadas a Lombok por Adrian el 2026-10-24, rama `feature/valoracion`). A mano → `Modulo`, `RolModulo`, `HistorialEtapas`, `OrdenEtapaFecha`, `ValoracionImagen`, `Observacion`, `ImagenObservacion`, `Notificaciones`, `PlantillaMensaje`.
- **Timestamps:** `createAt` + `updateAt` en `Cliente`, `Tecnico`, `Vehiculo`, `OrdenReparacion`, `Valoracion`; solo `createAt` en `Observacion`, `ImagenObservacion`, `ValoracionImagen`, `Notificaciones`; ninguno en `Usuario`.
- **Relaciones inversas mapeadas:** `Cliente.vehiculos`, `Vehiculo.ordenesReparacion`, `Modulo.rolesModulo`, `OrdenReparacion.{historialEtapas, ordenEtapaFechas, valoracion}`, `Valoracion.{detalles, imagenes}`, `Observacion.imagenes`.
- Ninguna relación usa `FetchType.LAZY`. Ninguna entidad tiene callbacks ni métodos de negocio.

### Longitudes vigentes

| Entidad | Columna | length |
|---|---|---|
| `Persona` | `documento` | 10 |
| `Persona` | `nombre_completo` | 200 |
| `Persona` | `celular` | 16 |
| `Persona` | `correo` | 100 |
| `Tecnico` | `especialidad` | 20 (enum) |
| `Usuario` | `nombre` / `email` / `password_hash` | 50 / 100 / 60 |
| `Vehiculo` | `placa` | 6 |
| `Vehiculo` | `marca` | 20 |
| `Vehiculo` | `modelo` | 10 |
| `Vehiculo` | `color` | 50 |
| `Vehiculo` | `vin` | 100 |
| `Etapas` | `nombre_etapa` | 100 |
| `Etapas` | `descripcion` | 255 |
| `Modulo` | `codigo` | 10 |
| `Modulo` | `nombre` | 100 |
| `Observacion` | `nota` | 500 |
| `ImagenObservacion` | `url` | 255 |
| `Valoracion` | `descripcion_general` | 500 |
| `ValoracionDetalle` | `pieza` | 255 |
| `ValoracionDetalle` | `observacion` | 255 |
| `ValoracionImagen` | `url` | 2083 |
| `ValoracionImagen` | `descripcion` | 300 |
| `PlantillaMensaje` | `evento`, `canal`, `contenido_template` | 255 |

- Sin `length` (default 255, rompen la convención): `Notificaciones.eventoDisparador`, `canal`, `contenidoEnviado`, `errorDetalle`.
- `Vehiculo.modelo` en 10: si es la línea completa (`Sandero Stepway`) se queda corto. Verificar contra el uso real.

### Esquema de la compartida (verificado 2026-10-26)
- Coincide con el modelo (18 tablas: columnas, tipos, nulabilidad, FKs, UNIQUE e índices); arrancar la app no ejecuta ningún `alter table`.
- 2026-10-26: se repuso `uk_orden_etapa UNIQUE(id_orden_reparacion, id_etapa)` en `orden_etapa_fecha` (se había perdido) y se eliminó `usuario.id_tecnico` con su FK y su UNIQUE.
- `valoracion_detalle.cantidad` existe como `SMALLINT NOT NULL` sin `DEFAULT 1` (el Service siempre la llena).

---

## 5. Bases de datos y datos de prueba

### Compartida (equipo)
- MariaDB 11.8.9 en el servidor del equipo, vía Tailscale (la URL está en el `.env` de cada integrante; no se escribe aquí porque el repo es público).
- Cuenta para probar: la cuenta ADMIN de Adrian (id 6); credenciales: pedírselas a Adrian.
- **Datos de trabajo permanentes (no borrar):** 2 usuarios ADMIN (ids 5 y 6); 10 clientes (`idPersona` 4–13); 10 vehículos (ids 1–10; placas DTR649, ZRX903, KTZ482, HDM217, JWR905, GXL364, FQN731, LMB058, NBK112, PEW274); 10 órdenes `ACTIVA` (OT 1–10, sin etapa ni fechas); 9 técnicos (`idPersona` 14–22: 2 ARMADOR, 2 LATONERO, 1 MECANICO, 1 ALISTADOR, 2 PINTOR, 1 CONTROL_CALIDAD); las 9 etapas.
- **Datos de prueba de Valoración (2026-10-26, Adrian pidió conservarlos):**

| OT | Vehículo | Estado | Valoración |
|---|---|---|---|
| 11 | 1 (DTR649) | ACTIVA | 1 pieza, cargada a CESVI, editada después de marcar |
| 12 | 2 (ZRX903) | CANCELADA | 1 pieza, sin cargar (no aparece en ningún filtro) |
| 13 | 3 (KTZ482) | CERRADA (ingreso y entrega el 2026-10-26, con observación de entrega) | 1 pieza, cargada a CESVI |
| 14 | 11 (PRB123, nuevo; dueño = cliente 23, nuevo) | CERRADA | Flujo completo hecho por Adrian en Postman: cliente → vehículo → orden → valoración → ingreso → días → etapas → observación → CESVI → entrega |

### Local (XAMPP, desde 2026-10-24)
- `jdbc:mariadb://localhost:3306/gestion_reparacion_colision`, **MariaDB 10.4**, usuario `root`. En el `.env` la línea de la compartida queda comentada.
- `ddl-auto` no puede alterar tablas en 10.4: las FKs y UNIQUE se aplicaron a mano desde `target/schema-modelo.sql` (sin la FK de `usuario.id_tecnico`, que no existe en local), más `uk_orden_etapa UNIQUE(id_orden_reparacion, id_etapa)`.
- 2026-10-31: CHECK de `valoracion_detalle.accion` rehecho a mano con `PINTAR`.
- **Datos de prueba locales:**
  - Usuarios: 6 (ADMIN, cuenta de Adrian), 7 `asesor@taller.local` (ASESOR), 8 `gerente@taller.local` (GERENTE). Las contraseñas de prueba no se escriben aquí (repo público): pedírselas a Adrian.
  - 9 etapas (ids 1–9 = orden 1–9) con especialidad y días límite.
  - Clientes `idPersona` 4–13 (13 inactivo). Técnicos 14–24 (23 PINTOR inactivo; 24 MECANICO comparte documento con el cliente 8 — rol doble).
  - Vehículos 1–11 (11 inactivo, placa `ABC12D`). Placas 1–10 iguales a la compartida.
  - Órdenes:

| OT | Estado | Situación | Sirve para |
|---|---|---|---|
| 1–3 | ACTIVA | Sin fechas | Fechas desde cero; 409 por falta de ingreso |
| 4 | ACTIVA | Solo `fechaIngresoCotizar` | 409 al re-marcar cotizar |
| 5 | ACTIVA | Ingreso 23-oct, 10 días, en "Asignado" | Mover a Desarme |
| 6 | ACTIVA | Ingreso 24-oct, sin días, en "Asignado" | Marcar días estimados |
| 7 | ACTIVA | Desarme (Pedro Pablo Arango) | Avanzar |
| 8 | ACTIVA | Latonería desde 19-oct (Wilson Giraldo) — **crítica** | Alertas; 409 al volver a Desarme |
| 9 | ACTIVA | Pintura, saltó Bancada — **vencida** (estimada 22-oct) | Volver a etapa saltada |
| 10 | ACTIVA | Listo para entregar — **próxima a vencer** (26-oct); la trae el cliente 4, el vehículo es del 12 | Probar `/entregar` |
| 11 | CERRADA | Entregada 17-sep, vehículo 1 (segunda orden del vehículo) | 409 por estado; columna Entregado |
| 12 | CANCELADA | Vehículo 2 | 409 por estado; reactivar |
| 13 | CERRADA | Vehículo 3, entregada a tiempo el 10-oct (estimada 12-oct) | Listado de entregadas |
| 14 | CERRADA | Vehículo 6, entregada con retraso el 20-oct (estimada 17-oct); la trajo el cliente 4 | Listado de entregadas |
| 15 | CERRADA | Vehículo 8, entregada el 29-ago desde Armado (sin nota de entrega) | Listado; entrega desde una etapa intermedia |

  - Las órdenes con etapa tienen su `orden_etapa_fecha` e `historial_etapas` coherentes, y hay 4 observaciones (una de entrega con `etapa = NULL`).
- Scripts de carga (temporales, del 2026-10-24): `constraints_local.sql` y `seed_local.sql` en el scratchpad de la sesión de Claude. Si se quieren conservar, moverlos a `docs/`.

---

## 6. Hallazgos

- **2026-10-24 — `OrdenEtapaFecha` no declara el `UNIQUE(id_orden_reparacion, id_etapa)`**: no aparece en el DDL generado. En la compartida existe (puesto a mano) y en la local se agregó como `uk_orden_etapa`. Pendiente declararlo en la entidad.
- **2026-10-24 — MariaDB local dañado por un cierre inesperado:** XAMPP no arrancaba ("MySQL shutdown unexpectedly"); con `mysqld --console` se vio `Can't open and lock privilege tables: Incorrect file format 'db'`. Se respaldó `data/mysql` (en el scratchpad), se restauró `mysql.db` desde `C:\xampp\mysql\backup\mysql` (solo tenía los permisos por defecto) y se repararon con `aria_chk -r` `procs_priv`, `event`, `global_priv` y `proxies_priv`. Usuarios, contraseñas y bases de datos quedaron intactos. Prevención: detener MySQL desde XAMPP antes de suspender o apagar el portátil.
- **2026-10-24 — `target/schema-modelo.sql` acumula corridas** de `./mvnw test`; la versión vigente es la que empieza en el último `create table`.
- **2026-10-26 — En la compartida faltaba el `UNIQUE(id_orden_reparacion, id_etapa)` de `orden_etapa_fecha`** que figuraba como puesto a mano. Repuesto como `uk_orden_etapa`. Mientras no se declare en la entidad, `ddl-auto` no lo recrea.
- **2026-10-31 — En MariaDB el CHECK que pone Hibernate a una columna enum es de columna, no una restricción con nombre:** `information_schema.CHECK_CONSTRAINTS` lo lista con el nombre de la columna, pero `ALTER TABLE ... DROP CONSTRAINT <columna>` falla ("no existe"). Se quita o rehace redefiniendo la columna: `ALTER TABLE t MODIFY col VARCHAR(20) NOT NULL [CHECK (...)]`. Comprobado en la local (10.4); en la compartida (11.8) revisar con `SHOW CREATE TABLE`.
- **2026-10-25 — Revisión completa de la API antes de conectar el front:** 9 errores (E1–E9, sección 2.0), diferencias de contrato con `grc-front` (sección 2.1), endpoints faltantes (2.2) y mejoras (2.3). Reporte en PDF en el Drive de Adrian (`Reporte de estado API GRC — 2026-10-25`).

---

## 7. Decisiones abiertas y fases futuras

- **Reproceso de una etapa ya visitada (pregunta para el taller):** cuando Control de calidad rechaza un trabajo, ¿basta con una observación o hay que registrar que el carro volvió a Pintura (y pagar el repintado)? Si es lo segundo ("versión C"): quitar el `UNIQUE(orden, etapa)` y el `existsBy...`, permitir varias filas por etapa (días en etapa = suma) y decidir si se limitan los ciclos. **Decidirlo antes del Dashboard y del cálculo de pagos.** Pasar de A a C no obliga a rehacer nada.
- **Usuario que entregó la orden:** sin nota no queda registro. Mejora futura: `OrdenReparacion.usuarioEntrega` (FK a `Usuario`, nullable, desde el token).
- **Unificar Lombok vs. accesores a mano** (decisión con Adrian): (a) quitar Lombok de las 7 entidades o (b) adoptarlo en todo el modelo.
- **Dónde guardar las fotos** (disco, S3, Cloudinary).
- Fase futura: técnico de campo con acceso web/móvil (habría que reponer el vínculo `Usuario` ↔ `Tecnico`); cálculo de pagos por etapa sobre `OrdenEtapaFecha.tecnico`; autoasignación del técnico.
- Documentación formal: hay un Product Backlog del MVP (30 historias en 8 épicas, Gherkin, 2026-09-16); faltan prioridades MoSCoW, estimación y las Reglas de Negocio numeradas.
- Nombre final del proyecto.

---

## 8. Requisitos por módulo (análisis 2026-09-10 al 16)

Decisiones confirmadas por pantalla. Las reglas transversales (etapas, fechas, entrega, alertas, acceso por rol) están en CLAUDE.md.

### Autenticación y control de acceso
- Login con `email` y contraseña → Dashboard de Alertas. Solo usuarios activos. Credenciales incorrectas → mensaje genérico.
- El menú muestra solo los módulos del rol; el acceso por URL directa a un módulo no permitido se bloquea.
- Cierre de sesión: vuelve al login; volver atrás exige autenticarse.
- Contraseñas sin autogestión: no hay autoregistro, activación ni "cambiar mi contraseña". La asigna quien crea el usuario; ante olvido, un ADMIN/GERENTE la reasigna.

### Dashboard de Alertas
- 6 tarjetas clicables: Vehículos en Taller, Fuera del Taller, Críticos, Vencidos, Próximos a Vencer, En Proceso. Clic → lista filtrada.
- "Vehículos en Taller" muestra "+X ingresos hoy"; los demás sub-datos del prototipo eran errores de maqueta.
- **Movimientos Recientes:** eventos de las últimas 24 h (5 visibles + "Ver más"): creación de orden, cambio de etapa, valoración cargada, nueva observación, marcado de fecha, marcado de CESVI. No incluye "notificación enviada".
- **Carga por Etapa:** Latonería, Pintura, Desarme, Electromecánica, Armado, Control de calidad y Alistamiento de superficies. Excluye Asignado, Listo para entregar y Entregado.

### Backlog Kanban
- Columnas: "Asignado" (calculada) + 9 etapas + "Entregado" (calculada, `CERRADA`).
- Al arrastrar, las columnas visitadas se muestran deshabilitadas.
- Tarjeta: placa, marca/modelo/año, cliente, "X días aquí" (con color de alerta), número de OT.
- Modal de cambio de etapa: técnico (filtrado por especialidad, salvo etapas sin especialidad), nota opcional (crea `Observacion`), checkbox notificar. Un solo botón "Guardar".
- Notificación: nota + checkbox → un solo mensaje combinado. Sin límite de mensajes.

### Ficha de Orden (5 pestañas)
- **Información:** Vehículo (marca, modelo, año, placa, color), Cliente (de `OrdenReparacion.cliente`), Etapa actual (etapa, técnico, "desde", asesor = último `HistorialEtapas.usuario`), Entrega (ingreso, estimada, días en taller, estado). "Días en taller" = desde `fechaIngresoReparacion` (vacío/0 si no hay).
- **Reparación:** 3 fichas de fechas (Ingreso a cotizar y Ingreso al taller con botón de calendario; Días estimados como campo numérico). 9 fichas de etapas + ficha "Entregado" (abre el modal de entrega). Estado visual: verde completada, naranja en curso, gris pendiente. Botón de lápiz → mismo modal del Kanban.
- **Valoración:** la misma pantalla del módulo Valoración.
- **Observaciones:** lista única de todos los orígenes, sin distinguir. Formulario: etapa (solo visitadas; "Entrega" solo si la orden está `CERRADA`), nota, imágenes sin límite, checkbox notificar; `ubicacion = EN_TALLER` automática. Se muestra autor, fecha/hora, badge "ENVIADO AL CLIENTE CON X FOTOS" si `visibleCliente`, y la nota.
- **Fotografías:** solo lectura. Todas las fotos (Valoración y Observaciones) con su origen ("Valoración", nombre de la etapa o "Entrega").

### Nueva Orden
- Formulario progresivo: placa (auto-búsqueda a los 6 caracteres) → si existe, muestra vehículo y dueño y pregunta "¿Es quien trae el vehículo?"; si no, o si responde "No", pide documento (auto-búsqueda) → si el cliente no existe, pide nombre, celular, correo → si el vehículo no existía, pide marca, modelo, año, color (VIN y año opcionales). Un solo botón "Crear orden".
- 4 combinaciones según existencia de placa/documento (crea Persona+Cliente, Vehiculo, OrdenReparacion según corresponda).
- Estado inicial `ACTIVA`; el número de OT es el `idOrdenReparacion` (correlativo sin prefijo).

### Técnicos
- Catálogo CRUD separado de Usuarios. ADMIN/GERENTE: CRUD; ASESOR: solo lista.
- Formulario: nombre, documento, celular, especialidad. Sin correo.
- Eliminar = desactivar; el historial no se toca y las órdenes en curso quedan con ese técnico hasta el siguiente cambio de etapa. Botón reactivar.
- Columna "En curso" = `OrdenEtapaFecha` abiertas del técnico.

### Usuarios
- Cuentas de login. Solo ADMIN/GERENTE, CRUD completo.
- Contraseña asignada por quien crea; reseteo desde Editar con campo opcional (vacío = no cambiar).
- Eliminar = desactivar (no puede iniciar sesión).
- El desplegable de rol **no ofrece `TECNICO`** (solo ADMIN, ASESOR, GERENTE). No pide técnico.

### Valoración
- Pantalla propia: búsqueda por placa + 3 filtros: **Sin valorar** (sin fila en `Valoracion`), **Sin cargar a CESVI** (`cargadaCesvi = false`), **Cargadas a CESVI**.
- Al entrar a una orden: sin valoración → "la hoja" vacía; con valoración → vista PDF + descarga ZIP de fotos + botón Editar.
- "Cargada a CESVI": botón manual en la vista PDF. Editar después no lo resetea.
- La hoja: descripción general + piezas (texto libre, `accion`, `gravedad` con REPARACION y PINTAR; mostradas en tres grupos, SUSTITUCIÓN → REPARACIÓN → PINTURA, alfabéticas por `pieza` dentro de cada uno) + fotos (de la valoración, sin descripción, sin límite, cámara o archivo).
- Guardado en un solo bloque; no se puede guardar sin al menos un `ValoracionDetalle`.
- ADMIN, ASESOR, GERENTE crean, editan y marcan CESVI.

### Fuera del MVP
- **Clientes** (marcado "PRÓXIMO" en el menú).

---

## 9. Bitácora

- **2026-09-05/06:** refactor de longitudes (fin del `VARCHAR(255)` genérico); `Persona` con `JOINED`; se quita el UNIQUE de `documento` (rol doble) y se agrega `idx_persona_documento`; `placa` pasa a UNIQUE (búsqueda de ~24,7 ms a ~1,7 ms); se eliminan primitivos, callbacks, cascadas de `Valoracion` y métodos de negocio de las entidades.
- **2026-09-10 al 16:** análisis de todos los módulos del MVP; Product Backlog (30 historias).
- **2026-09-11:** se agrega la etapa "Alistamiento de superficies".
- **2026-09-12:** se borra `GuiaDiagranaUML.pdf` del repo (`a7b80cb`); la guía se regenera desde `docs/`.
- **2026-09-13:** días límite por etapa; Valoración analizada.
- **2026-09-23:** enum `Especialidad`.
- **2026-09-27:** timestamps en `Cliente`, `Tecnico`, `Vehiculo`. CLAUDE.md se perdió en un pull/push del equipo.
- **2026-10-10:** búsqueda por placa y documento; `GlobalExceptionHandler` completo; documento duplicado validado.
- **2026-10-17:** celular E.164; `anio` opcional; `updateAt` nullable (solo al modificar; filas viejas pasadas a `NULL`); `OrdenReparacion` sin `tecnicoResponsable` (FK y columna borradas a mano); `UbicacionActual` descartado; CLAUDE.md deja de versionarse.
- **2026-10-18:** DTOs a `record` + `ResponseDto` (`a763d8c`); JWT (`eab142f`); capas de `OrdenReparacion` (`d66a543`, PR #6); `PlacaUtil`, `DocumentoUtil`; paso a la base compartida; PR #7 (Usuario: excepciones propias, `UsuarioUpdateDto`, `EmailUtil`, sin relación con `Tecnico`); `OrdenReparacion` y `Etapas` con Lombok; `ubicacionActual` en `OrdenReparacion`.
- **2026-10-20:** `Etapas` con `diasLimiteCritico` y `especialidad`, capas completas y catálogo cargado (`3a62605`, PR #8). Bancada y Listo para entregar sin técnico.
- **2026-10-22:** "Entregado" deja de ser etapa (fila id 10 borrada de la compartida) → evento `/entregar`; regla de movimiento "versión A"; cambio de etapa pasos 1–3 (`0dc66be`, PR #9 → `000afc7`).
- **2026-10-24:** BD local en XAMPP (la red de la universidad bloquea Tailscale): FKs/UNIQUE a mano y datos de prueba. Hallazgo del UNIQUE faltante en `OrdenEtapaFecha`. Se decide hacer las fechas del ciclo de vida antes de `/entregar`; Adrian las implementa y pasan 19 casos con curl. Reparación del MariaDB local. CLAUDE.md se separa en reglas (CLAUDE.md) y estado (API.md); **API.md sí se versiona** (repo público: sin contraseñas, IPs ni correos personales). Mismo día, más tarde: se repara el MariaDB local (tablas del sistema dañadas por un cierre inesperado); fechas, entrega, `GET /entregadas` y observaciones mergeados en `develop` (`8fe995b`); el login devuelve `token` + `usuario` (Williams, `c3706c5`); se borra `docs/` del repo (`5f2c7c1`); propuesta de almacenamiento de fotos; Valoración: campo `cantidad` (pedido por Adrian), crear y obtener hechos y probados en `feature/valoracion` (sin commit).
- **2026-10-25:** revisión completa del backend y contraste con `grc-front`. API.md se pone al día con lo mergeado (Valoración crear/obtener PR #18, paridad de usuarios, CORS central, "En curso" de técnicos). Se registran los errores E1–E9, las diferencias de contrato con el front (rutas, verbos, "vista de orden", formato de errores) y la lista de endpoints que faltan. Reporte en PDF subido al Drive.
- **2026-10-26:** Valoración: Adrian implementa editar (`PUT`, reemplazo completo), marcar CESVI (`PATCH /cesvi`), listados por filtro (`FiltroValoracion`) y conteo. Probado con curl en la compartida (OT 11–13 de prueba): editar A–E → A, B, F, G, H deja 5 piezas con ids nuevos; `createAt`/autor intactos; 404 sin valoración u orden; 409 por gravedad sin tocar las piezas; 400 con lista vacía; descripción en blanco → `null`; 409 en cancelada y cerrada; CESVI 200 → 409 al repetir, 404 sin valoración, 409 en cancelada, 200 en cerrada; editar tras CESVI conserva `cargadaCesvi`/`cargadaCesviAt`; conteo = largo de cada lista en cada paso; filtro inválido o en minúsculas → 400; **sin `filtro` → 500 (E10)**. Esquema de la compartida comparado contra el modelo: al día; se repone `uk_orden_etapa` y se elimina `usuario.id_tecnico`. Mismo día: Adrian agrega el handler de `MissingServletRequestParameterException`; sin `filtro` → 400 `"Falta el parámetro obligatorio: filtro"`.
- **2026-10-27:** historial de etapas (`feature/historial-etapas`): Adrian hace el repositorio, el DTO y el Service; Claude completa el Service, agrega `asesor` a `OrdenReparacionResponseDto` (después de `usuarioCreador`) y escribe `HistorialEtapasController`. Probado con curl en la compartida, solo lecturas: sin token 401; OT 14 → 8 cambios en orden cronológico (salta Electromecánica); OT 1 → `[]` y `asesor: null`; OT 99999 → 404; `abc` → 400; OT 13 (entregada sin etapas) → `[]`; `asesor` presente en `/api/orden` y `/entregadas`. Prueba en vivo con permiso de Adrian (usuario ASESOR 7, cliente 24, vehículo 12 `ZZZ999`, OT 15, todos borrados al terminar): sin cambios → `[]` y `asesor: null`; el ASESOR mueve a Bancada → 1 fila y `asesor` = el ASESOR con `usuarioCreador` = Adrian (campos no cruzados); Adrian mueve a Pintura → 2 filas en orden y `asesor` = Adrian. E5 corregido por Adrian (sin `@CrossOrigin` en `ObservacionController`).
- **2026-10-31:** Valoración: nueva acción **`PINTAR`** (`feature/valoracion-accion`). Adrian la agrega al enum `Accion`, reordenado a `SUSTITUCION, REPARACION, PINTAR` (el orden de declaración define el orden de la hoja); `validarDetalles` exige `gravedad` con `PINTAR`; `convertirADto` ordena con `Comparator.comparing(ValoracionDetalle::getAccion)` y luego por pieza. Claude corrige el mensaje de `accion` y el comentario de `gravedad` en `ValoracionDetalleRequestDto`. CLAUDE.md: lista de enums y regla de gravedad actualizadas. En la base local el CHECK de `accion` se rehace con `MODIFY` (hallazgo en la sección 6). Probado con curl en la local (OT 16): `PINTAR` sin gravedad → 409; `POST` con 6 piezas mezcladas → 201; `GET` → 200 en orden sustitución → reparación → pintar, alfabético en cada grupo. Valoración de prueba borrada y OT 16 restaurada (`fecha_ingreso_cotizar` y `update_at` en `NULL`). Pendiente: CHECK en la compartida y sección de pintura en el front.
- **2026-10-31:** etapas de la orden (`feature/etapas-orden`): a pedido de Adrian, Claude escribe `GET /api/orden/{idOrden}/etapas` (`OrdenEtapaFechaResponseDto`, `OrdenEtapaFechaService`, `OrdenEtapaFechaController` y `findByOrdenReparacionIdOrdenReparacionOrderByFechaInicioAsc` en el repositorio) para que el front muestre el técnico de cada etapa. Compila y `./mvnw test` arranca el contexto; falta probarlo contra la base (orden con etapas, `[]`, 404).
