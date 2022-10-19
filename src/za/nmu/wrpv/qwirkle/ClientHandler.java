package za.nmu.wrpv.qwirkle;

import za.nmu.wrpv.qwirkle.messages.Message;
import za.nmu.wrpv.qwirkle.messages.client.Stop;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class ClientHandler {
    private final Socket client;
    private final int clientID;
    private int gameID;

    private ObjectOutputStream ous;

    private final BlockingQueue<Message> clientMessages;

    private final ClientReader clientReader;
    private ClientWriter clientWriter;
    public ClientHandler(Socket client, int clientID, int gameID) {
        this.client = client;
        this.clientID = clientID;
        this.gameID = gameID;
        clientMessages = new LinkedBlockingDeque<>();

        clientReader = new ClientReader();
        clientReader.start();
    }

    private class ClientReader extends Thread{
        @Override
        public void run() {
            try {
                ous = new ObjectOutputStream(client.getOutputStream());
                ous.flush();
                ObjectInputStream ois = new ObjectInputStream(client.getInputStream());

                 clientWriter = new ClientWriter();
                 clientWriter.start();

                Message msg;
                do {
                    msg = (Message) ois.readObject();
                    msg.put("handler", ClientHandler.this);
                    msg.apply();
                }while (true);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                ClientHandler.this.stop();
            }
            finally {
                if (clientWriter != null)
                    clientWriter.interrupt();
            }
        }
    }

    private class ClientWriter extends Thread {
        @Override
        public void run() {
            try {
                do {
                    Message message = clientMessages.take();
                    ous.writeObject(message);
                    ous.flush();

                }while (true);
            } catch (InterruptedException e) {
                e.printStackTrace();
                clientWriter = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void send(Message message) {
        clientMessages.add(message);
    }

    public void stop() {
        System.out.println(">>> Stop -> clientID = " + getClientID());
        send(new Stop());
        clientReader.interrupt();
    }

    public int getClientID() {
        return clientID;
    }
}
