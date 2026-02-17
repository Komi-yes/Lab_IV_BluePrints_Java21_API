package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
    @GetMapping
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {
        Set<Blueprint> blueprints = services.getAllBlueprints();
        return ResponseEntity.ok(
                new ApiResponse<>(200, "execute ok", blueprints)
        );
    }

    // GET /api/v1/blueprints/{author}
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
            @NotBlank String author,
            @NotBlank String name,
            @Valid List<Point> points
    ) {}
}