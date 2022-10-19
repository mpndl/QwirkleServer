package za.nmu.wrpv.qwirkle;

import za.nmu.wrpv.qwirkle.messages.Message;
import za.nmu.wrpv.qwirkle.messages.client.Begin;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private int gameID;
    private final List<ClientHandler> handlers;

    public Game(int gameID) {
        this.handlers = new ArrayList<>();
        this.gameID = gameID;
    }

    public void add(ClientHandler handler) {
        if (!saturated()) {
            // Universal game subscriptions
            PubSubBroker.subscribe("messages" + gameID, (publisher, topic, message) -> handler.send((Message) message));
            handlers.add(handler);
        }
    }

    public boolean saturated() {
        return handlers.size() >= 4;
    }
    public boolean ready() {
        return handlers.size() >= 2;
    }

    public void start() {
        Message message = new Begin();
        PubSubBroker.publish(gameID, gameID + "", message);
    }

    public int playerCount() {
        return handlers.size();
    }
}
