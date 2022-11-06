package za.nmu.wrpv.qwirkle;

import za.nmu.wrpv.qwirkle.messages.client.Countdown;
import za.nmu.wrpv.qwirkle.messages.client.Info;
import za.nmu.wrpv.qwirkle.messages.client.Stop;
import za.nmu.wrpv.qwirkle.messages.client.Waiting;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class GamesHandler {
    public static final Map<Integer, Game> games = new ConcurrentHashMap<>();
    private static final BlockingQueue<ClientHandler> handlers = new LinkedBlockingQueue<>();
    public static CountdownThread countdownThread = null;
    public final static int timeout = 30000;
    public static int gameID = 0;
    private static Runnable onFinally = null;
    private static Runnable onInterrupted = null;
    private static boolean newGame = true;

    public synchronized static void start() {
        new GamesThread().start();
    }

    public synchronized static void add(ClientHandler handler) {
        handlers.add(handler);
    }

    public synchronized static void putGame(Game game) {
        games.putIfAbsent(game.gameID, game);
    }

    public synchronized static Game getGame(int gameID) {
        return games.get(gameID);
    }

    public synchronized static void removeGame(int gameID) {
        Game game = games.get(gameID);
        game.removeAll();
        games.remove(gameID);
    }

    public synchronized static void stopCountdown() {
        if (countdownThread != null && countdownThread.isAlive()) countdownThread.interrupt();
    }

    public synchronized static void put(ClientHandler handler) {
        handlers.add(handler);
    }

    public synchronized static void removeClient(int clientID) {
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
            do {
                try {
                    if (!games.containsKey(gameID)) {
                        System.out.println(">>> NEW GAME -> gameID = " + gameID);
                        newGame = true;
                    }
                    putGame(new Game(gameID));
                    Game game = getGame(gameID);

                    ClientHandler handler = handlers.take();

                    onFinally = () -> {
                        if(!game.began()) {
                            if (game.ready()) {
                                beginGame(game);
                                this.interrupt();
                            }
                        }
                    };

                    onInterrupted = () -> {
                        if (!game.began()) {
                            boolean removed = game.remove(handler.getClientID());
                            if (removed) {
                                System.out.println(">>> GAME " + handler.gameID + " LEFT -> clientID = " + handler.getClientID());
                                if (!game.ready()) {
                                    Waiting msg = new Waiting();
                                    PubSubBroker.publish(game.gameID, game.topic("wait"), msg);
                                    GamesHandler.stopCountdown();
                                }
                            }
                        }
                    };

                    if (newGame) {
                        countdownThread = new CountdownThread(onFinally, onInterrupted);
                        newGame = false;
                    }

                    if (!game.joined(handler.getClientID())) {
                        System.out.println(">>> JOINED -> clientID = " + handler.clientID + ", gameID = " + game.gameID);
                        initializeClient(handler, game);

                        if (game.ready() && !countingDown()) startCountdown(game);
                        else {
                            if (countingDown()) {
                                System.out.println(">>> SENDING CURRENT SECONDS = " + CountdownThread.getCurrentSeconds()
                                        + " TO clientID = " + handler.clientID);
                                Countdown message = new Countdown();
                                message.put("seconds", CountdownThread.getCurrentSeconds());
                                handler.send(message);
                            } else clientsWait(game);
                        }

                        if (game.saturated()) beginGame(game);
                    } else {
                        Stop message = new Stop();
                        message.put("handler", handler);
                        message.apply();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }

    private synchronized static void initializeClient(ClientHandler handler, Game game) {
        handler.setGameID(game.gameID);
        game.add(handler);

        Info message = new Info();
        message.put("name", Game.getPlayerName());
        message.put("gameID", game.gameID);
        message.put("clientID", handler.getClientID());
        handler.send(message);
        handler.name = (String) message.get("name");
    }

    private synchronized static void startCountdown(Game game) {
        countdownThread = new CountdownThread(onFinally, onInterrupted);
        countdownThread.start();
        Countdown message = new Countdown();
        message.put("seconds", countdownThread.getTimeoutMills() / 1000);
        System.out.println(">>> SECONDS -> " + message.get("seconds"));
        PubSubBroker.publish(game.gameID, game.topic("countdown"), message);
    }

    public synchronized static void resetCountdown(ClientHandler handler, Game game) {
        Countdown message = new Countdown();
        message.put("seconds", CountdownThread.getCurrentSeconds());
        handler.send(message);
        startCountdown(game);
    }

    private synchronized static void clientsWait(Game game) {
        System.out.println(">>> WAIT -> gameID = " + game.gameID);
        Waiting message = new Waiting();
        PubSubBroker.publish(game.gameID, game.topic("wait"), message);
        stopCountdown();
    }

    private synchronized static void beginGame(Game game) {
        stopCountdown();
        game.begin();
    }

    public synchronized static boolean countingDown() {
        return countdownThread.isAlive();
    }
}
