package edu.eci.arsw.blueprints;

import edu.eci.arsw.blueprints.filters.BlueprintsFilter;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.persistence.impl.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.impl.BlueprintPersistence;
import edu.eci.arsw.blueprints.persistence.impl.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlueprintsServicesTest {

    @Mock
    private BlueprintPersistence persistence;

    @Mock
    private BlueprintsFilter filter;

    @InjectMocks
    private BlueprintsServices services;

    private Blueprint testBlueprint;
    private Set<Blueprint> testBlueprints;

    @BeforeEach
    void setUp() {
        testBlueprint = TestDataBuilder.createBlueprint("author1", "blueprint1");
        testBlueprints = TestDataBuilder.createBlueprintsSet();
    }

    @Test
    void shouldAddNewBlueprintSuccessfully() throws BlueprintPersistenceException {
        // When
        services.addNewBlueprint(testBlueprint);

        // Then
        verify(persistence).saveBlueprint(testBlueprint);
    }

    @Test
    void shouldThrowExceptionWhenAddingExistingBlueprint() throws BlueprintPersistenceException {
        // Given
        doThrow(new BlueprintPersistenceException("Blueprint already exists")).when(persistence).saveBlueprint(any());

        // When & Then
        assertThrows(BlueprintPersistenceException.class, () -> services.addNewBlueprint(testBlueprint));
        verify(persistence).saveBlueprint(testBlueprint);
    }

    @Test
    void shouldGetAllBlueprints() {
        // Given
        when(persistence.getAllBlueprints()).thenReturn(testBlueprints);

        // When
        Set<Blueprint> result = services.getAllBlueprints();

        // Then
        assertEquals(testBlueprints, result);
        verify(persistence).getAllBlueprints();
    }

    @Test
    void shouldGetBlueprintsByAuthor() throws BlueprintNotFoundException {
        // Given
        Set<Blueprint> authorBlueprints = TestDataBuilder.createBlueprintsSetForAuthor("author1");
        when(persistence.getBlueprintsByAuthor("author1")).thenReturn(authorBlueprints);

        // When
        Set<Blueprint> result = services.getBlueprintsByAuthor("author1");

        // Then
        assertEquals(authorBlueprints, result);
        verify(persistence).getBlueprintsByAuthor("author1");
    }

    @Test
    void shouldThrowExceptionWhenAuthorNotFound() throws BlueprintNotFoundException {
        // Given
        when(persistence.getBlueprintsByAuthor("unknown")).thenThrow(new BlueprintNotFoundException("Author not found"));

        // When & Then
        assertThrows(BlueprintNotFoundException.class, () -> services.getBlueprintsByAuthor("unknown"));
        verify(persistence).getBlueprintsByAuthor("unknown");
    }

    @Test
    void shouldGetBlueprintByAuthorAndName() throws BlueprintNotFoundException {
        // Given
        Blueprint filteredBlueprint = TestDataBuilder.createBlueprint("author1", "blueprint1");
        when(persistence.getBlueprint("author1", "blueprint1")).thenReturn(testBlueprint);
        when(filter.apply(testBlueprint)).thenReturn(filteredBlueprint);

        // When
        Blueprint result = services.getBlueprint("author1", "blueprint1");

        // Then
        assertEquals(filteredBlueprint, result);
        verify(persistence).getBlueprint("author1", "blueprint1");
        verify(filter).apply(testBlueprint);
    }

    @Test
    void shouldThrowExceptionWhenBlueprintNotFound() throws BlueprintNotFoundException {
        // Given
        when(persistence.getBlueprint("author1", "unknown")).thenThrow(new BlueprintNotFoundException("Blueprint not found"));

        // When & Then
        assertThrows(BlueprintNotFoundException.class, () -> services.getBlueprint("author1", "unknown"));
        verify(persistence).getBlueprint("author1", "unknown");
        verify(filter, never()).apply(any());
    }

    @Test
    void shouldAddPointToBlueprint() throws BlueprintNotFoundException {
        // When
        services.addPoint("author1", "blueprint1", 15, 25);

        // Then
        verify(persistence).addPoint("author1", "blueprint1", 15, 25);
    }

    @Test
    void shouldThrowExceptionWhenAddingPointToNonExistentBlueprint() throws BlueprintNotFoundException {
        // Given
        doThrow(new BlueprintNotFoundException("Blueprint not found")).when(persistence).addPoint(any(), any(), anyInt(), anyInt());

        // When & Then
        assertThrows(BlueprintNotFoundException.class, () -> services.addPoint("author1", "unknown", 15, 25));
        verify(persistence).addPoint("author1", "unknown", 15, 25);
    }
}