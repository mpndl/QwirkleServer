package za.nmu.wrpv.qwirkle;

import za.nmu.wrpv.qwirkle.messages.Message;
import za.nmu.wrpv.qwirkle.messages.client.Begin;

import java.util.ArrayList;
import java.util.List;

public class Game {
    public int gameID;
    public boolean timeOut = false;
    public List<ClientHandler> handlers;
    public GameModel model;
    public boolean began = false;

    public Game(int gameID) {
        this.handlers = new ArrayList<>();
        this.gameID = gameID;
    }

    public void add(ClientHandler handler) {
        if (!saturated()) {
            // Universal game subscriptions
            if (!handlers.contains(handler)) {
                handler.subscriber = (publisher, topic, message) -> handler.send((Message) message);
                PubSubBroker.subscribe("begin", handler.subscriber);
                PubSubBroker.subscribe("messages", handler.subscriber);
                PubSubBroker.subscribe("played", handler.subscriber);
                PubSubBroker.subscribe("drawn", handler.subscriber);
                PubSubBroker.subscribe("forfeit", handler.subscriber);
                PubSubBroker.subscribe("stop", handler.subscriber);
                PubSubBroker.subscribe("countdown", handler.subscriber);
                PubSubBroker.subscribe("wait", handler.subscriber);
                PubSubBroker.subscribe("ended", handler.subscriber);
                handlers.add(handler);
            }
        }
    }

    public boolean saturated() {
        return handlers.size() >= 4;
    }
    public boolean ready() {
        return handlers.size() >= 2;
    }

    public void begin() {
        model = new GameModel(playerCount());

        Player currentPlayer = model.getCurrentPlayer();
        List<Tile> bag = model.getBag();
        List<Player> players = model.getPlayers();

        Message message = new Begin();
        message.put("currentPlayerIndex", model.getPlayerIndex(currentPlayer));
        message.put("bag", bag);
        message.put("players", players);
        PubSubBroker.publish(gameID, "begin", message);

        began = true;
    }

    public int playerCount() {
        return handlers.size();
    }

    public boolean remove(int clientID) {
        boolean removed = false;
        for (ClientHandler handler: (ArrayList<ClientHandler>) ((ArrayList<ClientHandler>)handlers).clone()) {
            if (handler.getClientID() == clientID) {
                removed = handlers.remove(handler);
                if (removed) {
                    PubSubBroker.unsubscribe(handler.subscriber);
                    System.out.println(">>> REMOVED -> clientID = " + clientID);
                }
            }
        }
        return removed;
    }

    public void removeAll() {
        for (ClientHandler handler: (ArrayList<ClientHandler>) ((ArrayList<ClientHandler>)handlers).clone()) {
            handlers.remove(handler);
            PubSubBroker.unsubscribe(handler.subscriber);
        }
    }
}
