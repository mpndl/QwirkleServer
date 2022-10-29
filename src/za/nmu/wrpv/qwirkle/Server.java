package za.nmu.wrpv.qwirkle;

import za.nmu.wrpv.qwirkle.messages.client.Countdown;
import za.nmu.wrpv.qwirkle.messages.client.Name;
import za.nmu.wrpv.qwirkle.messages.client.Waiting;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    public static void main(String[] args) throws IOException {
        new Server();
    }

    private static final Map<Integer, Game> games = new ConcurrentHashMap<>();
    public static boolean countingDown = false;
    public static int pID = 0;
    public static int timeOut = 30000;
    public static Thread countThread = null;
    public static int currentSeconds = timeOut/1000;
    public static int gameID = 0;
    Server() throws IOException {
        int clientID = 0;

        ClientHandler handler;
        ServerSocket server = new ServerSocket(5051);

        System.out.printf(">>> RUNNING -> address = %s, port = %d", InetAddress.getLocalHost().getHostAddress() ,server.getLocalPort());
        System.out.println();
        do {
            if (!games.containsKey(gameID)) System.out.println(">>> NEW GAME -> gameID = " + gameID);
            putGame(new Game(gameID));
            Game game = getGame(gameID);
            try {
                if (game.ready()) {
                    server.setSoTimeout(timeOut);
                    countingDown = true;
                    countThread = new Thread(() -> {
                        try {
                            do {
                                Thread.sleep(1000);
                                --currentSeconds;
                            }while (currentSeconds > 0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }finally {
                            currentSeconds = timeOut/1000;
                            countingDown = false;
                        }
                    });
                    countThread.start();
                }
                else
                    server.setSoTimeout(0);

                Socket client = server.accept();
                clientID++;
                pID++;

                System.out.println(">>> CONNECTED -> clientID = " + clientID);
                handler = new ClientHandler(client, clientID, gameID);

                game.add(handler);

                Name name = new Name();
                name.put("name", "PLAYER" + pID);
                name.put("gameID", gameID);
                handler.send(name);
                handler.playerName = (String) name.get("name");

                if (game.ready() && !countingDown) {
                    Countdown message = new Countdown();
                    message.put("seconds", timeOut / 1000);
                    System.out.println(">>> SECONDS -> " + message.get("seconds"));
                    PubSubBroker.publish(gameID, game.topic("countdown"), message);
                    countingDown = true;
                }else {
                    if (countingDown) {
                        Countdown message = new Countdown();
                        message.put("seconds", currentSeconds);
                        handler.send(message);
                    }else {
                        Waiting message = new Waiting();
                        PubSubBroker.publish(gameID, game.topic("wait"), message);
                    }
                }

                if (game.saturated())
                    throw  new SocketTimeoutException();
                else {
                    game.add(handler);
                }
            }catch (SocketTimeoutException e) {
                if (game.ready()) {
                    game.begin();
                    System.out.println(">>> GAME STARTED -> gameID = " + gameID + ", playerCount = " + game.clientCount());
                    gameID++;
                    pID = 0;
                    countingDown = false;
                    countThread.interrupt();
                }
            }
        } while (true);
    }

    private static byte[] toByte(String fileName) {
        File file = new File(fileName);
        if (file.exists() && !file.isDirectory()) {
            try {
                return Files.readAllBytes(file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void putGame(Game game) {
        games.putIfAbsent(game.gameID, game);
    }

    public static Game getGame(int gameID) {
        return games.get(gameID);
    }

    public static void removeGame(int gameID) {
        games.remove(gameID);
    }

    public static void interrupt() {
        if (countThread != null && countThread.isAlive()) {
            countThread.interrupt();
            countThread = null;
        }
    }
}
