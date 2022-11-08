package za.nmu.wrpv.qwirkle;

import za.nmu.wrpv.qwirkle.messages.Message;
import za.nmu.wrpv.qwirkle.messages.client.Begin;
import za.nmu.wrpv.qwirkle.messages.client.Joined;
import za.nmu.wrpv.qwirkle.messages.client.Stop;
import za.nmu.wrpv.qwirkle.messages.server.Join;

import java.util.ArrayList;
import java.util.List;
public class Game {
    private static int pID = 0;
    public int gameID;
    public List<ClientHandler> handlers;
    public GameModel model;
    private boolean began = false;

    public Game(int gameID) {
        this.handlers = new ArrayList<>();
        this.gameID = gameID;
    }

    public static String getPlayerName() {
        return "PLAYER"+pID;
    }

    private boolean contains(int clientID) {
        return handlers.stream().anyMatch(handler -> handler.prevClientID == clientID);
    }

    public boolean joined(int clientID) {
        for (ClientHandler handler: handlers) {
            if (handler.clientID == clientID) return true;
        }
        return false;
    }

    public void add(ClientHandler handler) {
        if (!saturated()) {
            // Universal game subscriptions
            if (!handlers.contains(handler)) {
                if (pID < 4) pID++;

                handler.subscriber = (publisher, topic, message) -> handler.send((Message) message);
                PubSubBroker.subscribe(topic("begin"), handler.subscriber);
                PubSubBroker.subscribe(topic("messages"), handler.subscriber);
                PubSubBroker.subscribe(topic("played"), handler.subscriber);
                PubSubBroker.subscribe(topic("drawn"), handler.subscriber);
                PubSubBroker.subscribe(topic("forfeit"), handler.subscriber);
                PubSubBroker.subscribe(topic("stop"), handler.subscriber);
                PubSubBroker.subscribe(topic("countdown"), handler.subscriber);
                PubSubBroker.subscribe(topic("wait"), handler.subscriber);
                PubSubBroker.subscribe(topic("ended"), handler.subscriber);
                PubSubBroker.subscribe(topic("joined"), handler.subscriber);
                PubSubBroker.subscribe(topic("sync"), handler.subscriber);
                handlers.add(handler);
            }
        }
    }

    public void notifyJoined(Player player) {
        Joined message = new Joined();
        message.put("player", player);
        message.put("players", model.players);
        message.put("currentPlayerIndex", model.getPlayerIndex(model.currentPlayer));
        message.put("currentPlayerName", model.currentPlayer.name.toString());
        PubSubBroker.publish(gameID, topic("joined"), message);
    }

    public boolean rejoin(int clientID, ClientHandler rejoin) {
        if (began()) {
            System.out.println(">>> ATTEMPTING REJOIN -> clientID = " + clientID);
            for (ClientHandler handler : handlers) {
                System.out.println(">>> SEARCHING -> clientID = " + handler.clientID);
                if (handler.getClientID() == clientID) {
                    System.out.println(">>> FOUND -> clientID = " + clientID);
                    Player player;
                    try {
                        player = model.restoreRemovedPlayer(handler.name);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }

                    GamesHandler.removeClient(clientID);
                        rejoin.name = handler.name;

                        remove(clientID);

                        Begin message = new Begin();
                        message.put("currentPlayerIndex", model.getPlayerIndex(model.currentPlayer));
                        message.put("currentPlayerName", model.currentPlayer.name);
                        message.put("bag", model.getBag());
                        message.put("players", model.getPlayers());
                        message.put("board", model.board);
                        message.put("player", player);
                        message.put("name", rejoin.name);
                        message.put("placed", model.placed);
                        message.put("messages", model.messages);

                        rejoin.send(message);
                        notifyJoined(GameModel.clonePlayer(player));
                        add(rejoin);

                        System.out.println(">>> REJOINED -> old clientID = " + handler.clientID + ", new clientID = " + rejoin.clientID + ", gameID = " + gameID);
                        return true;
                }
            }
        }
        System.out.println(">>> NOT REJOINED -> clientID = " + rejoin.clientID + ", gameID = " + gameID);
        return false;
    }

    public boolean saturated() {
        return handlers.size() >= 4;
    }
    public boolean ready() {
        return handlers.size() >= 2;
    }

    public void begin() {
        pID = 0;
        GamesHandler.gameID++;
        began = true;

        if (clientCount() >= 2) {
            System.out.println(">>> STARTING GAME - > gameID = " + gameID);
            model = new GameModel(clientCount());

            Player currentPlayer = model.getCurrentPlayer();
            List<Tile> bag = model.getBag();
            List<Player> players = model.getPlayers();

            Begin message = new Begin();
            message.put("currentPlayerIndex", model.getPlayerIndex(currentPlayer));
            message.put("bag", bag);
            message.put("players", players);
            message.put("board", model.board);
            message.put("placed", model.placed);
            message.put("messages", model.messages);
            PubSubBroker.publish(gameID, topic("begin"), message);
            System.out.println(">>> GAME STARTED -> gameID = " + gameID + ", playerCount = " + handlers.size());
        }else {
            PubSubBroker.publish(gameID, topic("stop"), new Stop());
            removeAll();
            GamesHandler.removeGame(gameID);
        }
    }

    public int clientCount() {
        return handlers.stream().filter(ClientHandler::running).toList().size();
    }

    public boolean remove(int clientID) {
        boolean removed = false;
        for (ClientHandler handler: (ArrayList<ClientHandler>) ((ArrayList<ClientHandler>)handlers).clone()) {
            if (handler.getClientID() == clientID) {
                removed = handlers.remove(handler);
                if (removed) {
                    if (pID > 0) pID--;
                    Player temp = new Player();
                    temp.name = Player.Name.valueOf(handler.name);
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
        pID = 0;
    }

    public String topic(String topic) {
        return topic + gameID;
    }

    public boolean began() {
        return began;
    }
}
