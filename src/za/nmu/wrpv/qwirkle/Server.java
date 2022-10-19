package za.nmu.wrpv.qwirkle;

import za.nmu.wrpv.qwirkle.messages.client.Name;
import za.nmu.wrpv.qwirkle.messages.client.Waiting;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;

public class Server {
    public static void main(String[] args) throws IOException {
        new Server();
    }

    public static Game game = new Game();
    Server() throws IOException {
        int clientID = 0;
        int gameID = 0;
        int timeOut = 5000;
        int pID = 0;

        ClientHandler handler;
        ServerSocket server = new ServerSocket(5050);

        System.out.printf(">>> RUNNING -> port = %d",server.getLocalPort());
        do {
            try {
                game.setGameID(gameID);
                if (game.ready())
                    server.setSoTimeout(timeOut);
                else
                    server.setSoTimeout(0);

                Socket client = server.accept();
                clientID++;
                pID++;

                System.out.println("\n>>> CONNECTED -> clientID = " + clientID);
                handler = new ClientHandler(client, clientID, gameID);

                Name name = new Name();
                name.put("name", "PLAYER" + pID);
                handler.send(name);

                handler.send(new Waiting());

                if (game.saturated())
                    throw  new SocketTimeoutException();
                else {
                    game.add(handler);
                }
            }catch (SocketTimeoutException e) {
                game.begin();
                System.out.println(">>> GAME STARTED -> gameID = " + gameID + ", playerCount = " + game.playerCount());
                gameID++;
                game = new Game();
                pID = 0;
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
}
