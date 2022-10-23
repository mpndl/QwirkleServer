package za.nmu.wrpv.qwirkle;

import za.nmu.wrpv.qwirkle.messages.client.Name;
import za.nmu.wrpv.qwirkle.messages.client.Waiting;

import java.io.File;
import java.io.IOException;
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
    public static boolean ready = false;
    public static int pID = 0;
    Server() throws IOException {
        int clientID = 0;
        int gameID = 0;
        int timeOut = 10000;

        ClientHandler handler;
        ServerSocket server = new ServerSocket(5050);

        System.out.printf(">>> RUNNING -> port = %d",server.getLocalPort());
        System.out.println();
        do {
            if (!games.containsKey(gameID)) System.out.println(">>> NEW GAME -> gameID = " + gameID);
            putGame(new Game(gameID));
            Game game = getGame(gameID);
            try {
                if (game.ready())
                    server.setSoTimeout(timeOut);
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
                handler.send(name);

                Waiting message = new Waiting();
                if (game.ready() && !ready) {
                    message.put("seconds", timeOut / 1000);
                    System.out.println(">>> SECONDS -> " + message.get("seconds"));
                    ready = true;
                }
                PubSubBroker.publish(gameID, "restart", message);

                if (game.saturated())
                    throw  new SocketTimeoutException();
                else {
                    game.add(handler);
                }
            }catch (SocketTimeoutException e) {
                if (game.ready()) {
                    game.begin();
                    System.out.println(">>> GAME STARTED -> gameID = " + gameID + ", playerCount = " + game.playerCount());
                    gameID++;
                    pID = 0;
                    ready = false;
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
}
