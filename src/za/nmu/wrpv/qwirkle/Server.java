package za.nmu.wrpv.qwirkle;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

public class Server {
    public static void main(String[] args) throws IOException {
        new Server();
    }

    Server() throws IOException {
        int clientID = 0;
        ServerSocket server = new ServerSocket(5050);

        System.out.printf(">>> RUNNING -> port = %d",server.getLocalPort());

        do {
            Socket client = server.accept();
            clientID++;

            System.out.println("\n>>> CONNECTED -> clientID = " + clientID);

            new ClientHandler(client, clientID);
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
