package za.nmu.wrpv.qwirkle;

import za.nmu.wrpv.qwirkle.messages.client.Countdown;
import za.nmu.wrpv.qwirkle.messages.client.Info;
import za.nmu.wrpv.qwirkle.messages.client.Waiting;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class GamesHandler {
    private static final Map<Integer, Game> games = new ConcurrentHashMap<>();
    private static final BlockingQueue<ClientHandler> handlers = new LinkedBlockingQueue<>();
    public static CountdownThread countdownThread = null;
    public final static int timeout = 5000;
    public static int gameID = 0;

    public static void start() {
        new GamesThread().start();
    }

    public static void add(ClientHandler handler) {
        handlers.add(handler);
    }

    public static void putGame(Game game) {
        games.putIfAbsent(game.gameID, game);
    }

    public static Game getGame(int gameID) {
        return games.get(gameID);
    }

    public static void removeGame(int gameID) {
        Game game = games.get(gameID);
        game.removeAll();
        games.remove(gameID);
    }

    public static void stopCountdown() {
        if (countdownThread.isAlive()) countdownThread.interrupt();
    }

    public static void put(ClientHandler handler) {
        handlers.add(handler);
    }

    public static void removeClient(int clientID) {
        // rejoining client
        Game game = getGame(gameID);
        if (game != null) {
            game.remove(clientID);
            if (!game.ready()) stopCountdown();
        }
    }

    static class GamesThread extends Thread {
        @Override
        public void run() {
            try {
                do {
                    if (!games.containsKey(gameID)) {
                        System.out.println(">>> NEW GAME -> gameID = " + gameID);
                        countdownThread = new CountdownThread(() -> beginGame(getGame(gameID)));
                    }
                    putGame(new Game(gameID));
                    Game game = getGame(gameID);

                    ClientHandler handler = handlers.take();

                    if (!game.joined(handler.getClientID())) {
                        System.out.println(">>> JOINED -> clientID = " + handler.clientID + ", gameID = " + game.gameID);
                        initializeClient(handler, game);

                        if (game.ready() && !countingDown()) startCountdown(game);
                        else {
                            if (countingDown()) resetCountdown(handler, game);
                            else clientsWait(game);
                        }

                        if (game.saturated()) beginGame(game);
                    }

                } while (true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void initializeClient(ClientHandler handler, Game game) {
        handler.setGameID(game.gameID);
        game.add(handler);

        Info message = new Info();
        message.put("name", Game.getPlayerName());
        message.put("gameID", game.gameID);
        message.put("clientID", handler.getClientID());
        handler.send(message);
        handler.name = (String) message.get("name");
    }

    private static void startCountdown(Game game) {
        countdownThread = new CountdownThread(() -> beginGame(getGame(gameID)));
        countdownThread.start();
        Countdown message = new Countdown();
        message.put("seconds", countdownThread.getTimeoutMills() / 1000);
        System.out.println(">>> SECONDS -> " + message.get("seconds"));
        PubSubBroker.publish(game.gameID, game.topic("countdown"), message);
    }

    public static void resetCountdown(ClientHandler handler, Game game) {
        Countdown message = new Countdown();
        message.put("seconds", CountdownThread.getCurrentSeconds());
        handler.send(message);
        startCountdown(game);
    }

    private static void clientsWait(Game game) {
        Waiting message = new Waiting();
        PubSubBroker.publish(game.gameID, game.topic("wait"), message);
        stopCountdown();
    }

    private static void beginGame(Game game) {
        game.begin();
        System.out.println(">>> GAME STARTED -> gameID = " + game.gameID + ", playerCount = " + game.clientCount());
        stopCountdown();
    }

    public static boolean countingDown() {
        return countdownThread.isAlive();
    }
}
