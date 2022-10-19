package za.nmu.wrpv.qwirkle;

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

    Server() throws IOException {
        int clientID = 0;
        int gameID = 0;
        Game game = new Game(gameID);
        ClientHandler handler;
        ServerSocket server = new ServerSocket(5050);

        System.out.printf(">>> RUNNING -> port = %d",server.getLocalPort());
        do {
            try {
                if (game.ready())
                    server.setSoTimeout(30000);
                else
                    server.setSoTimeout(0);

                Socket client = server.accept();
                clientID++;

                System.out.println("\n>>> CONNECTED -> clientID = " + clientID);

                if (game.saturated())
                    throw  new SocketTimeoutException();
                else {
                    handler = new ClientHandler(client, clientID, gameID);
                    game.add(handler);
                }
            }catch (SocketTimeoutException e) {
                game.start();
                System.out.println(">>> GAME STARTED -> gameID = " + gameID + ", playerCount = " + game.playerCount());
                gameID++;
                game = new Game(gameID);
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
