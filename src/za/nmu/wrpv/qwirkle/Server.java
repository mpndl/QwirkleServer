package za.nmu.wrpv.qwirkle;

import za.nmu.wrpv.qwirkle.messages.client.Info;
import za.nmu.wrpv.qwirkle.messages.client.Stop;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    public static void main(String[] args) throws IOException {
        new Server();
    }

    public static Map<Integer, ClientHandler> connectionReadyHandlers = new ConcurrentHashMap<>();
    public static int clientID = 0;
    Server() throws IOException {
        ClientHandler handler;
        ServerSocket server = new ServerSocket(5051);

        System.out.printf(">>> RUNNING -> address = %s, port = %d", InetAddress.getLocalHost().getHostAddress() ,server.getLocalPort());
        GamesHandler.start();
        System.out.println();
        do {
            Socket client = server.accept();
            handler = new ClientHandler(client, clientID);
            System.out.println(">>> CONNECTED -> clientID = " + handler.clientID);

            clientID++;

            Info info = new Info();
            info.put("clientID", handler.getClientID());
            handler.send(info);

            connectionReadyHandlers.put(handler.clientID ,handler);
        } while (true);
    }

    public static void join(ClientHandler handler) {
        GamesHandler.put(handler);
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
