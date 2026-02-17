package edu.eci.arsw.blueprints.persistence;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@Primary
public class PostgresBlueprintPersistence implements BlueprintPersistence {

    private final JpaBlueprintRepository repo;

    public PostgresBlueprintPersistence(JpaBlueprintRepository repo) {
        this.repo = repo;
    }

    @Override
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {
        if (repo.findByAuthorAndName(bp.getAuthor(), bp.getName()).isPresent()) {
            throw new BlueprintPersistenceException("Blueprint already exists: "
                    + bp.getAuthor() + "/" + bp.getName());
        }
        repo.save(bp);
    }

    @Override
    public Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException {
        return repo.findByAuthorAndName(author, name)
                .orElseThrow(() -> new BlueprintNotFoundException(
                        "Blueprint not found: %s/%s".formatted(author, name)));
    }

    @Override
    public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {
        List<Blueprint> list = repo.findByAuthor(author);
        if (list.isEmpty()) throw new BlueprintNotFoundException(
                "No blueprints for author: " + author);
        return new HashSet<>(list);
    }

    @Override
    public Set<Blueprint> getAllBlueprints() {
        return new HashSet<>(repo.findAll());
    }

    @Override
    public void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException {
        Blueprint bp = getBlueprint(author, name);
        bp.addPoint(new Point(x, y));
        repo.save(bp);
    }
}