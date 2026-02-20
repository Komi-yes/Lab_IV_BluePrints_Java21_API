package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.impl.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.impl.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1/blueprints")
public class BlueprintsAPIController {
    private final BlueprintsServices services;
    public BlueprintsAPIController(BlueprintsServices services) {
        this.services = services;
    }
    // GET /api/v1/blueprints
    @Operation(
            summary = "Obtener todos los blueprints",
            description = "Retorna todos los blueprints almacenados."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta exitosa",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content)
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {
        Set<Blueprint> blueprints = services.getAllBlueprints();
        return ResponseEntity.ok(
                new ApiResponse<>(200, "execute ok", blueprints)
        );
    }
    // GET /api/v1/blueprints/{author}
    @Operation(
            summary = "Obtener blueprints por autor",
            description = "Retorna los blueprints asociados a un autor."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta exitosa",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Autor no encontrado o sin blueprints",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content)
    })
    @GetMapping("/{author}")
    public ResponseEntity<ApiResponse<?>> byAuthor(@PathVariable String author) {
        try {
            Set<Blueprint> blueprints = services.getBlueprintsByAuthor(author);
            return ResponseEntity.ok(
                    new ApiResponse<>(200, "execute ok", blueprints)
            );
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(404, e.getMessage(), null)
            );
        }
    }
    // GET /api/v1/blueprints/{author}/{bpname}
    @Operation(
            summary = "Obtener blueprint por autor y nombre",
            description = "Retorna un blueprint específico identificado por autor y nombre."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta exitosa",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content)
    })
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<ApiResponse<?>> byAuthorAndName(
            @PathVariable String author,
            @PathVariable String bpname) {
        try {
            Blueprint bp = services.getBlueprint(author, bpname);
            return ResponseEntity.ok(
                    new ApiResponse<>(200, "execute ok", bp)
            );
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(404, e.getMessage(), null)
            );
        }
    }
    // POST /api/v1/blueprints
    @Operation(
            summary = "Crear un nuevo blueprint",
            description = "Crea un blueprint con autor, nombre y lista de puntos."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Blueprint creado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Blueprint ya existe o no se puede persistir",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Error de validación en la solicitud",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<ApiResponse<?>> add(@Valid @RequestBody NewBlueprintRequest req) {
        try {
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ApiResponse<>(201, "Blueprint created successfully", null)
            );
        } catch (BlueprintPersistenceException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ApiResponse<>(403, e.getMessage(), null)
            );
        }
    }
    // PUT /api/v1/blueprints/{author}/{bpname}/points
    @Operation(
            summary = "Agregar punto a un blueprint",
            description = "Agrega un punto (x,y) a un blueprint existente."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Punto agregado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content)
    })
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<ApiResponse<?>> addPoint(
            @PathVariable String author,
            @PathVariable String bpname,
            @RequestBody Point p) {
        try {
            services.addPoint(author, bpname, p.x(), p.y());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                    new ApiResponse<>(202, "Point added successfully", null)
            );
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(404, e.getMessage(), null)
            );
        }
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ApiResponse<>(400, "Validation error: " + message, null)
        );
    }
    public record ApiResponse<T>(int code, String message, T data) {}

    public record NewBlueprintRequest(
            @NotBlank(message = "author must not be blank") String author,
            @NotBlank(message = "name must not be blank") String name,
            @NotEmpty(message = "points must not be empty") @Valid List<Point> points
    ) {}
}
