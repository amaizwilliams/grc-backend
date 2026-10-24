# API.md — estado del proyecto

Bitácora y estado vivo del backend: qué está hecho, qué sigue, pendientes, datos de prueba, hallazgos, requisitos por módulo e historia. **Las reglas de cómo trabajamos y las convenciones están en `CLAUDE.md`**; aquí va lo que cambia con cada sesión.

Al cerrar un bloque se actualizan: "Retomar aquí", "Pendientes", "Estado de los módulos" y la "Bitácora".

**Última verificación contra el código:** 2026-10-24 (módulos y entidades verificados el 2026-10-20; cambios posteriores anotados en la bitácora).

---

## 1. Retomar aquí (actualizado 2026-10-24, fin de la sesión)

**Repo:** `develop` = `8fe995b` (fechas, entrega, entregadas y observaciones ya mergeados). Rama en curso: **`feature/valoracion`**, todo **sin commit** todavía. `main` va muy atrás de `develop`.

**Base en uso:** la **local** (XAMPP, MariaDB 10.4), porque en la universidad la red bloquea Tailscale. Ver sección 5.

### En curso: Valoración (sin fotos) — rama `feature/valoracion`
**Hecho y probado (20 casos con curl, base local restaurada después):**
- `ALTER TABLE valoracion_detalle ADD COLUMN cantidad SMALLINT NOT NULL DEFAULT 1;` aplicado **solo en la base local**. **Falta en la compartida** (hacerlo a mano antes de arrancar con este código contra ella).
- Entidad `ValoracionDetalle.cantidad` (`Short`, default 1). `Valoracion` y `ValoracionDetalle` pasadas a **Lombok** por Adrian (avisar a Williams).
- Repositorios: `ValoracionRepository` (`findBy...`/`existsByOrdenReparacionIdOrdenReparacion`, `findByCargadaCesvi`), `ValoracionDetalleRepository` (`deleteByValoracionIdValoracion`), y `OrdenReparacionRepository.findByEstadoAndValoracionIsNull`.
- DTOs en `dto/valoracion/`: `ValoracionRequestDto` (`descripcionGeneral`, `detalles` con `@NotEmpty` + `List<@Valid ...>`), `ValoracionDetalleRequestDto` (`pieza`, `accion`, `gravedad`, `cantidad` `Integer` 1–999 opcional, `observacion`), y sus `ResponseDto`.
- `ValoracionService`: `crearValoracion` (orden 404 → `validarActiva` → ya tiene valoración 409 → `validarDetalles` → guarda valoración y piezas → marca `fechaIngresoCotizar` si falta) y `obtenerValoracion` (404 si la orden no existe o no tiene valoración). Privados: `validarDetalles`, `guardarDetalles`, `limpiar`, `convertirADto` (SUSTITUCION primero, alfabético por pieza) y `convertirDetalleADto`.
- En `OrdenReparacionService` pasaron a `public`: `validarActiva` y `aplicarIngresoCotizar`.
- `ValoracionController` (sin `@RequestMapping` de clase, ruta completa por método): `POST` y `GET /api/orden/{idOrden}/valoracion`.

**Falta (pasos 4c–4e y 5 de la guía):**
1. `editarValoracion` → `PUT /api/orden/{idOrden}/valoracion`: mismas validaciones que crear (salvo: sin valoración → 404); reemplazo completo de piezas (`deleteBy...` + `guardarDetalles`); actualiza `descripcionGeneral` y `updateAt`; **no** toca `cargadaCesvi`. Para la respuesta, usar la lista recién guardada, no `valoracion.getDetalles()`.
2. `marcarCargadaCesvi` → `PATCH /api/orden/{idOrden}/valoracion/cesvi`: sin valoración → 404; ya marcada → 409; `cargadaCesvi = true` + `cargadaCesviAt`.
3. Listados: `GET /api/valoracion/sin-valorar` (órdenes `ACTIVA` sin valoración; requiere hacer `public` `OrdenReparacionService.convertirADto`) y `GET /api/valoracion?cargadaCesvi=true|false`.
4. Pruebas finales (edición reemplaza piezas, CESVI 200/409, editar tras CESVI no lo resetea, listados cambian de grupo).

**Antes del commit:**
- Cambiar el mensaje de `validarActiva` a "solo se puede modificar una orden activa" (hoy dice "marcar fechas" y lo ve también la valoración).
- Descartar `ObservacionRepository` (solo tiene una línea en blanco): `git restore src/main/java/gestion/reparabilidad/colision/repository/ObservacionRepository.java`.
- `@CrossOrigin(origins = "*")` en `ValoracionController` si el front lo necesita (como los demás).

**Decisiones por defecto en Valoración (Adrian no las contradijo):** costos (`costo`, `costoEstimado`) fuera de los DTOs; `gravedad` obligatoria con REPARACION y prohibida con SUSTITUCION (409); `cantidad` en las dos acciones; crear/editar solo en `ACTIVA`; piezas sin `DELETE` propio (se eliminan reemplazando la lista en el `PUT`).

### Planeado: almacenamiento de fotos (propuesta 2026-10-24, sin confirmar)
Disco local del servidor de Adrian (fuera de carpetas públicas, ruta en `FOTOS_DIR`), archivos con nombre UUID, en la base la ruta relativa; interfaz `AlmacenamientoService` para poder pasar a MinIO/S3; al subir: límite de tamaño, verificar tipo real, re-codificar, quitar EXIF (GPS), redimensionar y miniatura; acceso por **URLs firmadas HMAC de corta duración** (`/api/archivos/...?exp=&firma=`), que también sirven para WhatsApp; exponer el servidor con Cloudflare Tunnel; respaldo diario a disco externo + copia fuera de casa; UPS. **Pendiente que Adrian confirme:** el enfoque, cómo expone el servidor (¿dominio?), SO/disco del servidor y el volumen de fotos (estimado ~1,2 GB/mes con 30 fotos × 100 órdenes).

### Lo que hace Williams
Control por rol (`hasAnyRole` en `SecurityConfig`) — ver pendientes de Seguridad.

---

## 2. Pendientes vigentes

**1. Usuario / Auth (compañero)**
- [ ] `findByActivoTrue/False` + `GET /desactivados` + `PATCH /{id}/reactivar`.
- [ ] No aceptar el rol `TECNICO` en alta/edición.
- [ ] Mover `LoginRequest` a `dto/auth` (menor).
- [ ] Quitar de la base compartida la columna `usuario.id_tecnico` y su FK (ver sección 4).

**2. Seguridad**
- [ ] **Control por rol** según la tabla de acceso de CLAUDE.md (`hasAnyRole` en `SecurityConfig`). **Lo hace Williams (2026-10-24).**
- [ ] CORS: bean `CorsConfigurationSource` y quitar los `@CrossOrigin("*")` de todos los controllers (ver sección 3).
- [ ] Usuario semilla (`CommandLineRunner`) para cualquier base nueva.

**3. Modelo y catálogos**
- [ ] Declarar `UNIQUE(id_orden_reparacion, id_etapa)` en `OrdenEtapaFecha` (`@Table(uniqueConstraints = ...)`) — hoy solo existe en la base (ver Hallazgos).
- [ ] Tablas `modulo` y `plantilla_mensaje` vacías.
- [ ] `length` en `Notificaciones` (`evento_disparador`, `canal`, `contenido_enviado`, `error_detalle`); evaluar `TEXT` para `contenido_enviado` / `contenido_template`.
- [ ] (Recomendado) `FetchType.LAZY` en las relaciones (hoy 0).
- [ ] Igualar `ImagenObservacion.url` (255) con `ValoracionImagen.url` (2083).
- [ ] `Modulo.codigo` en 10 queda justo para códigos como `DETALLE_OT`.

**4. Correcciones menores en módulos hechos**
- [ ] Placa repetida (también de un vehículo desactivado): buscar con `findByPlaca` antes y dar un mensaje claro / ofrecer reactivar (hoy 409 genérico).
- [ ] Órdenes: ¿dos órdenes `ACTIVA` para el mismo vehículo? ¿validar vehículo/cliente activos al crear?
- [ ] Quitar `mysql-connector-j` del `pom.xml` (ojo: el `application.properties` de test usa `MySQLDialect`).
- [ ] Quitar el `@JsonIgnore` de `Cliente.vehiculos`.
- [ ] Formato de los `record` de `dto/etapas` (Ctrl+Alt+L).
- [ ] Técnicos: conteo de `OrdenEtapaFecha` abiertas por técnico (columna "En curso").

**5. Módulos de negocio**
- [x] `Etapas` (catálogo, 2026-10-20).
- [x] Cambio de etapa, pasos 1–3 (2026-10-22, PR #9).
- [x] Fechas del ciclo de vida (2026-10-24, rama `feature/fechas-orden`): 19 casos con curl OK en la base local.
- [x] Entrega (2026-10-24, rama `feature/entregar-orden`): 12 casos con curl OK en la base local (ruta implementada: `PATCH /api/orden/{id}/entregar-orden`).
- [x] `GET /api/orden/entregadas` (2026-10-24, rama `feature/entregar-orden`): `findByEstadoOrderByFechaDeEntregaRealDesc(CERRADA)`, más reciente primero. Probado. A futuro: limitar por días o paginar (la lista crece sin fin).
- [ ] Mensaje de `validarActiva` (ya es `public` y lo usan fechas, entrega y valoración): todavía dice "solo se pueden marcar fechas en una orden activa" → cambiar a "solo se puede modificar una orden activa".
- [ ] Sincronizar `ubicacionActual` al crear una `Observacion` con ubicación.
- [x] Observaciones (solo texto) (2026-10-24, rama `feature/observaciones`): `POST`/`GET /api/orden/{idOrden}/observaciones` con `ObservacionesService` + `ObservacionController`. Etapa solo visitada (o `NULL` = Entrega, solo en `CERRADA`); `CANCELADA` → 409; ubicación y estado de repuestos opcionales: si viene ubicación actualiza `ubicacionActual` (si no, hereda la de la orden). 21 casos con curl OK + regresión de etapa, entrega y fechas. **Decisión de ubicación opcional tomada por la recomendación; Adrian no la contradijo.**
- [ ] Imágenes de observaciones (bloqueado: decidir dónde se guardan las fotos).
- [ ] **Valoración (sin fotos)** ← EN CURSO (rama `feature/valoracion`): crear y obtener hechos y probados; faltan editar, CESVI y listados (ver sección 1).
- [ ] Fotos, PDF y ZIP de `Valoracion` (bloqueado por el almacenamiento de fotos) (la primera valoración marca `fechaIngresoCotizar` si falta; `gravedad` solo con `REPARACION`).
- [ ] Dashboard de alertas; `Notificaciones` WhatsApp `@Async` + `PlantillaMensaje`; `Modulo`/`RolModulo`.
- **Decisión abierta:** almacenamiento de fotos — hay propuesta (disco local + URLs firmadas, ver sección 1), falta que Adrian la confirme. Bloquea las imágenes de Observaciones y de Valoración.

**6. Operación / producción**
- [ ] Flyway en vez de `ddl-auto=update` (urgente con la base compartida); `show-sql=false`; `open-in-view=false`; paginación; HTTPS con proxy; respaldos.
- [ ] Llevar `develop` a `main`.

---

## 3. Estado de los módulos (API)

Todos los DTOs son `record`; ningún controlador devuelve una entidad.

| Módulo | Ruta | Endpoints |
|---|---|---|
| `Auth` | `/api/auth` | `POST /login` → `{"token": "...", "usuario": UsuarioResponseDto}` (desde `c3706c5`, del compañero) |
| `Usuario` | `/api/usuario` | GET lista (activos, filtrados en memoria), GET `/{id}`, POST (201), PUT `/{id}`, DELETE (204, lógico). Sin `/desactivados` ni `/reactivar` |
| `Cliente` | `/api/cliente` | POST, PUT `/{id}`, GET `/{id}`, GET `/documento/{documento}`, GET lista, GET `/desactivados`, DELETE (lógico), PATCH `/{id}/reactivar` |
| `Tecnico` | `/api/tecnico` | GET lista, GET `/desactivados`, GET `/{id}`, GET `/especialidad/{especialidad}` (activos; valor inválido o en minúsculas → 400), POST, PUT `/{id}`, DELETE (lógico), PATCH `/{id}/reactivar` |
| `Vehiculo` | `/api/vehiculo` | GET `/{id}`, GET `/placa/{placa}`, GET lista, GET `/desactivados`, POST, DELETE (lógico), PATCH `/{id}/reactivar`. Sin update (a propósito) |
| `OrdenReparacion` | `/api/orden` | POST, PUT `/{id}` (solo `ACTIVA`), DELETE `/{id}` (→ `CANCELADA`), PATCH `/{id}/reactivar` (solo desde `CANCELADA`), GET `/{id}`, GET (activas), GET `/entregadas` (CERRADA, la más reciente primero), GET `/placa/{placa}`, PATCH `/{id}/etapa`, PATCH `/{id}/ingreso-cotizar`, PATCH `/{id}/ingreso-reparacion`, PATCH `/{id}/dias-estimados`, PATCH `/{id}/entregar-orden` |
| `Etapas` | `/api/etapa` | GET lista (por `orden`), GET `/{id}`, POST, PUT `/{id}`. Sin DELETE, a propósito |
| `Observacion` | `/api/orden/{idOrden}/observaciones` | POST (201), GET (la más reciente primero). `ObservacionesService` + `ObservacionController` |
| `Valoracion` | `/api/orden/{idOrden}/valoracion` | POST (201), GET. **En curso:** PUT, PATCH `/cesvi`, `GET /api/valoracion/sin-valorar`, `GET /api/valoracion?cargadaCesvi=` |

Infraestructura:
- `config/SecurityConfig` + `config/JwtAuthenticationFilter` + `config/SecurityErrorWriter`, `service/JwtService`, `service/CustomUserDetailsService`.
- `exception/`: `RecursoNoEncontradoException`, `ReglaNegocioException`, `EmailAlreadyExistsException`, `CredencialesInvalidasException`, `UsuarioInactivoException`, `GlobalExceptionHandler` (probado contra el servidor).
- `util/`: `CelularUtil` (E.164), `DocumentoUtil` (quita espacios y puntos), `PlacaUtil` (quita espacios y guiones, mayúsculas), `EmailUtil` (`trim().toLowerCase()`).
- Solo `OrdenReparacionService` usa `@Transactional`.

### Seguridad
- **JWT funcionando.** `csrf` desactivado, sesión `STATELESS`, públicas solo `/api/auth/login` y `/error`; lo demás exige `Bearer` (sin token → 401 `"No autorizado. Debes iniciar sesión."`).
- Token: email como `subject`, `rol` como claim, expira en `jwt.expiration` (3600 s).
- `AuthService.login`: normaliza el email, valida `activo`, compara con BCrypt; email o contraseña incorrectos → mismo 401 genérico.
- **Falta control por rol** (cualquier autenticado entra a todo). `CustomUserDetailsService` ya carga el rol como authority.
- Quedan 7 controladores con `@CrossOrigin(origins = "*")` (todos menos `EtapasController`; `ObservacionController` lo trae desde 2026-10-24).

### Cambio de etapa (`PATCH /api/orden/{id}/etapa`, 2026-10-22)
- Repositorios: `OrdenEtapaFechaRepository` con `existsByOrdenReparacionIdOrdenReparacionAndEtapaIdEtapa` y `findByOrdenReparacionIdOrdenReparacionAndFechaFinIsNull` (`Optional`); `HistorialEtapasRepository` y `ObservacionRepository` vacíos.
- `CambioEtapaRequestDto(idEtapaDestino @NotNull, idTecnico, nota @Size(max=500), visibleCliente)`.
- `OrdenReparacionService.cambiarEtapa`: bloque A de validaciones (orden 404, `ACTIVA` 409, `fechaIngresoReparacion` 409, etapa 404, ya visitada 409, técnico sobra/falta/inactivo/especialidad 409, inexistente 404) y bloque B de escrituras con un solo `ahora`. Observación solo si la nota no está en blanco, con `visibleCliente = Boolean.TRUE.equals(...)`.
- Probado con curl (18 casos) en la compartida: `fechaFin` de cada etapa = `fechaInicio` de la siguiente, una sola etapa abierta, nota con `trim`. Volver a una etapa saltada (Pintura → Bancada) → 200.
- `TecnicoService.buscarTecnico` y `EtapasService.buscarEtapa` son públicos.
- La notificación WhatsApp (`@Async`) queda para después.

### Detalles por módulo
- **Cliente:** `documento` acepta 6–10 dígitos con punto o espacio entre grupos (`1.017.171.717`); se guarda solo dígitos. El guion se rechaza (en un NIT separa el dígito de verificación). Duplicado → 409, solo entre clientes. `GET /documento/{documento}` devuelve también inactivos. `ClienteUpdateDto` no trae `documento`; es PUT completo (un `correo` omitido queda `NULL`).
- **Tecnico:** mismas reglas de documento; duplicado → 409 solo entre técnicos. `TecnicoUpdateDto` no trae `documento` ni `activo`. Formulario sin `correo` (queda `NULL`).
- **Vehiculo:** la placa acepta espacios alrededor y un espacio o guion (`abc 123`, `ABC-123`) → se guarda `ABC123`. Placa repetida → 409 genérico del UNIQUE (también si está desactivado).
- **Usuario:** `UsuarioRequestDto` (nombre 3–50, email, rol, password 8–60 con mayúscula, minúscula y número); `UsuarioUpdateDto` con `password` opcional (vacía = no cambiar). Nunca expone `passwordHash`. Todavía acepta el rol `TECNICO`.
- **OrdenReparacion:** `OrdenReparacionRequestDto(idVehiculo, idCliente)`; el cliente puede ser distinto al dueño del vehículo. `OrdenReparacionResponseDto` anida vehículo y cliente, `usuarioCreador` como nombre, etapa como `idEtapaActual` + `nombreEtapaActual` (`null` = "Asignado") y todas las fechas. Se permiten dos órdenes `ACTIVA` por vehículo y no se valida que vehículo o cliente estén activos (pendiente).
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

### Restos en la base compartida
- `usuario.id_tecnico` (nullable, UNIQUE) con la FK `FKrysvt4lckwcbraswwmqp9paix`, sin uso. Quitar a mano: `ALTER TABLE usuario DROP FOREIGN KEY FKrysvt4lckwcbraswwmqp9paix; ALTER TABLE usuario DROP COLUMN id_tecnico;` (primero la FK). No ejecutado.

---

## 5. Bases de datos y datos de prueba

### Compartida (equipo)
- MariaDB 11.8.9 en el servidor del equipo, vía Tailscale (la URL está en el `.env` de cada integrante; no se escribe aquí porque el repo es público).
- Cuenta para probar: la cuenta ADMIN de Adrian (id 6); credenciales: pedírselas a Adrian.
- **Datos de trabajo permanentes (no borrar):** 2 usuarios ADMIN (ids 5 y 6); 10 clientes (`idPersona` 4–13); 10 vehículos (ids 1–10; placas DTR649, ZRX903, KTZ482, HDM217, JWR905, GXL364, FQN731, LMB058, NBK112, PEW274); 10 órdenes `ACTIVA` (OT 1–10, sin etapa ni fechas); 9 técnicos (`idPersona` 14–22: 2 ARMADOR, 2 LATONERO, 1 MECANICO, 1 ALISTADOR, 2 PINTOR, 1 CONTROL_CALIDAD); las 9 etapas.

### Local (XAMPP, desde 2026-10-24)
- `jdbc:mariadb://localhost:3306/gestion_reparacion_colision`, **MariaDB 10.4**, usuario `root`. En el `.env` la línea de la compartida queda comentada.
- `ddl-auto` no puede alterar tablas en 10.4: las FKs y UNIQUE se aplicaron a mano desde `target/schema-modelo.sql` (sin la FK de `usuario.id_tecnico`, que no existe en local), más `uk_orden_etapa UNIQUE(id_orden_reparacion, id_etapa)`.
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
- La hoja: descripción general + piezas (texto libre, `accion`, `gravedad` solo con REPARACION; mostradas SUSTITUIR arriba y REPARAR abajo, alfabéticas por `pieza`) + fotos (de la valoración, sin descripción, sin límite, cámara o archivo).
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
