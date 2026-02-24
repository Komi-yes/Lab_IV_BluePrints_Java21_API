package edu.eci.arsw.blueprints;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.eci.arsw.blueprints.controllers.BlueprintsAPIController;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.persistence.impl.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.impl.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import edu.eci.arsw.blueprints.socket.SocketIOClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.websocket.servlet.WebSocketMessagingAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BlueprintsAPIController.class,
        excludeAutoConfiguration = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                WebSocketMessagingAutoConfiguration.class
        })
class BlueprintsAPIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BlueprintsServices services;

    @MockBean
    private SocketIOClientService socketIOClientService;

    @Autowired
    private ObjectMapper objectMapper;

    private Set<Blueprint> testBlueprints;
    private Blueprint testBlueprint;

    @BeforeEach
    void setUp() {
        testBlueprints = TestDataBuilder.createBlueprintsSet();
        testBlueprint = TestDataBuilder.createBlueprint("author1", "blueprint1");
    }

    @Test
    void shouldGetAllBlueprints() throws Exception {
        // Given
        when(services.getAllBlueprints()).thenReturn(testBlueprints);

        // When & Then
        mockMvc.perform(get("/api/v1/blueprints"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("execute ok"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(testBlueprints.size()));
    }

    @Test
    void shouldGetBlueprintsByAuthor() throws Exception {
        // Given
        Set<Blueprint> authorBlueprints = TestDataBuilder.createBlueprintsSetForAuthor("author1");
        when(services.getBlueprintsByAuthor("author1")).thenReturn(authorBlueprints);

        // When & Then
        mockMvc.perform(get("/api/v1/blueprints/author1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("execute ok"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(authorBlueprints.size()));
    }

    @Test
    void shouldReturnNotFoundWhenAuthorDoesNotExist() throws Exception {
        // Given
        when(services.getBlueprintsByAuthor("unknown"))
                .thenThrow(new BlueprintNotFoundException("Author not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/blueprints/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Author not found"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void shouldGetBlueprintByAuthorAndName() throws Exception {
        // Given
        when(services.getBlueprint("author1", "blueprint1")).thenReturn(testBlueprint);

        // When & Then
        mockMvc.perform(get("/api/v1/blueprints/author1/blueprint1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("execute ok"))
                .andExpect(jsonPath("$.data.author").value("author1"))
                .andExpect(jsonPath("$.data.name").value("blueprint1"))
                .andExpect(jsonPath("$.data.points").isArray())
                .andExpect(jsonPath("$.data.points.length()").value(3));
    }

    @Test
    void shouldReturnNotFoundWhenBlueprintDoesNotExist() throws Exception {
        // Given
        when(services.getBlueprint("author1", "unknown"))
                .thenThrow(new BlueprintNotFoundException("Blueprint not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/blueprints/author1/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Blueprint not found"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void shouldCreateNewBlueprint() throws Exception {
        // Given
        when(services.getAllBlueprints()).thenReturn(Set.of());

        // When & Then
        mockMvc.perform(post("/api/v1/blueprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestDataBuilder.createValidJsonBlueprint()))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("Blueprint created successfully"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingInvalidBlueprint() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/blueprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestDataBuilder.createInvalidJsonBlueprint()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Validation error: author must not be blank"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void shouldReturnForbiddenWhenBlueprintAlreadyExists() throws Exception {
        // Given: el servicio falla al persistir porque ya existe
        doThrow(new BlueprintPersistenceException("Blueprint already exists"))
                .when(services).addNewBlueprint(any(Blueprint.class));

        // When & Then
        mockMvc.perform(post("/api/v1/blueprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestDataBuilder.createValidJsonBlueprint()))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("Blueprint already exists"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void shouldAddPointToBlueprint() throws Exception {
        // Given
        when(services.getBlueprint("author1", "blueprint1")).thenReturn(testBlueprint);

        // When & Then
        mockMvc.perform(put("/api/v1/blueprints/author1/blueprint1/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestDataBuilder.createJsonPoint()))
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(202))
                .andExpect(jsonPath("$.message").value("Point added successfully"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void shouldReturnNotFoundWhenAddingPointToNonExistentBlueprint() throws Exception {
        doThrow(new BlueprintNotFoundException("Blueprint not found"))
                .when(services).addPoint("author1", "unknown", 15, 25);

        // When & Then
        mockMvc.perform(put("/api/v1/blueprints/author1/unknown/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestDataBuilder.createJsonPoint()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Blueprint not found"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void shouldHandleValidationErrors() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/blueprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "author": "",
                                  "name": "",
                                  "points": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", allOf(
                        containsString("Validation error:"),
                        containsString("author must not be blank"),
                        containsString("name must not be blank"),
                        containsString("points must not be empty")
                )))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}