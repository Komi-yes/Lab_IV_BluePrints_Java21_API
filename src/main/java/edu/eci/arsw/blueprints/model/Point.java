package edu.eci.arsw.blueprints.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

@Embeddable
public class Point {

    private int x;
    private int y;

    protected Point() {}

    @JsonCreator
    public Point(@JsonProperty("x") int x,
                 @JsonProperty("y") int y) {
        this.x = x;
        this.y = y;
    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        try {
            json.put("x", this.x);
            json.put("y", this.y);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return json;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public int x() { return x; }
    public int y() { return y; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point p)) return false;
        return x == p.x && y == p.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
