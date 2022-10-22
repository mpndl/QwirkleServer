package za.nmu.wrpv.qwirkle;

import za.nmu.wrpv.qwirkle.messages.Message;
import za.nmu.wrpv.qwirkle.messages.client.Begin;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private int gameID;
    private final List<ClientHandler> handlers;

    public Game() {
        this.handlers = new ArrayList<>();
    }

    public void add(ClientHandler handler) {
        if (!saturated()) {
            // Universal game subscriptions
            PubSubBroker.subscribe("begin", (publisher, topic, message) -> handler.send((Message) message));
            PubSubBroker.subscribe("messages", (publisher, topic, message) -> handler.send((Message) message));
            PubSubBroker.subscribe("played", (publisher, topic, message) -> handler.send((Message) message));
            PubSubBroker.subscribe("drawn", (publisher, topic, message) -> handler.send((Message) message));
            handlers.add(handler);
        }
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }

    public boolean saturated() {
        return handlers.size() >= 4;
    }
    public boolean ready() {
        return handlers.size() >= 2;
    }

    public void begin() {
        GameModel model = new GameModel(playerCount());

        Player currentPlayer = model.getCurrentPlayer();
        List<Tile> bag = model.getBag();
        List<Player> players = model.getPlayers();

        Message message = new Begin();
        message.put("currentPlayer", currentPlayer);
        message.put("bag", bag);
        message.put("players", players);
        PubSubBroker.publish(gameID, "begin", message);
    }

    public int playerCount() {
        return handlers.size();
    }

    public String gameTopic(String topic) {
        return topic+gameID;
    }

    public void remove(int clientID) {
        for (ClientHandler handler: (ArrayList<ClientHandler>) ((ArrayList<ClientHandler>)handlers).clone()) {
            if (handler.getClientID() == clientID)
                handlers.remove(handler);
        }
    }
}
