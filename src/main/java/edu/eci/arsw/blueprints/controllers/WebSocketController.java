package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.BlueprintUpdate;
import edu.eci.arsw.blueprints.model.DrawEvent;
import edu.eci.arsw.blueprints.socket.SocketIOClientService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate template;
    private final SocketIOClientService socketIOClient;

    public WebSocketController(SimpMessagingTemplate template, SocketIOClientService socketIOClient) {
        this.template = template;
        this.socketIOClient = socketIOClient;
    }

    @MessageMapping("/draw")
    public void onDraw(DrawEvent evt) {
        var upd = new BlueprintUpdate(evt.author(), evt.name(), List.of(evt.point()));
        template.convertAndSend("/topic/blueprints." + evt.author() + "." + evt.name(), upd);

        socketIOClient.sendDrawEvent(evt.author(), evt.name(), evt.point());
    }
}
