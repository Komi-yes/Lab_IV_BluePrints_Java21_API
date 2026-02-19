package edu.eci.arsw.blueprints;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;

import java.util.List;
import java.util.Set;

public class TestDataBuilder {

    public static Point createPoint(int x, int y) {
        return new Point(x, y);
    }

    public static List<Point> createPointsList() {
        return List.of(
                createPoint(0, 0),
                createPoint(10, 10),
                createPoint(20, 20)
        );
    }

    public static Blueprint createBlueprint(String author, String name) {
        return new Blueprint(author, name, createPointsList());
    }

    public static Blueprint createBlueprintWithPoints(String author, String name, List<Point> points) {
        return new Blueprint(author, name, points);
    }

    public static Blueprint createEmptyBlueprint(String author, String name) {
        return new Blueprint(author, name, List.of());
    }

    public static Set<Blueprint> createBlueprintsSet() {
        return Set.of(
                createBlueprint("author1", "blueprint1"),
                createBlueprint("author1", "blueprint2"),
                createBlueprint("author2", "blueprint3")
        );
    }

    public static Set<Blueprint> createBlueprintsSetForAuthor(String author) {
        return Set.of(
                createBlueprint(author, "blueprint1"),
                createBlueprint(author, "blueprint2")
        );
    }

    public static String createValidJsonBlueprint() {
        return """
            {
                "author": "test_author",
                "name": "test_blueprint",
                "points": [
                    {"x": 0, "y": 0},
                    {"x": 10, "y": 10},
                    {"x": 20, "y": 20}
                ]
            }
            """;
    }

    public static String createInvalidJsonBlueprint() {
        return """
            {
                "author": "",
                "name": "test_blueprint",
                "points": [
                    {"x": 0, "y": 0},
                    {"x": 10, "y": 10}
                ]
            }
            """;
    }

    public static String createJsonPoint() {
        return """
            {
                "x": 15,
                "y": 25
            }
            """;
    }
}