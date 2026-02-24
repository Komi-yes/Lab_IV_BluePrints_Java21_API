package edu.eci.arsw.blueprints.socket;

import edu.eci.arsw.blueprints.model.Point;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URISyntaxException;

@Service
public class SocketIOClientService {

    @Value("${socketio.url:http://localhost:3001}")
    private String socketIoUrl;

    private Socket socket;

    @PostConstruct
    public void init() {
        try {
            socket = IO.socket(socketIoUrl);
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.out.println("Connected to Socket.IO server");
                }
            });

            socket.on("blueprint-update", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.out.println("Received blueprint update: " + args[0]);
                    // Handle blueprint updates from Node.js server
                }
            });

            socket.connect();

        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    @PreDestroy
    public void cleanup() {
        if (socket != null) {
            socket.disconnect();
        }
    }


    public void sendDrawEvent(String author, String name, Point point) {
        if (socket != null && socket.connected()) {
            String room = "blueprints." + author + "." + name;
            try {
                JSONObject eventData = new JSONObject();
                eventData.put("room", room);
                eventData.put("point", point.toJSONObject());
                eventData.put("author", author);
                eventData.put("name", name);
                eventData.put("fromSpring", true);

                socket.emit("draw-event", eventData);
            } catch (JSONException e) {
                System.err.println("Error creating JSON for Socket.IO event: " + e.getMessage());
            }
        }
    }
}
