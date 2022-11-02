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
    private Socket client;
    public int clientID;
    public int gameID;
    public Subscriber subscriber;
    public String name;

    private ObjectOutputStream ous;

    private final BlockingQueue<Message> clientMessages;

    private ClientReader clientReader;
    private ClientWriter clientWriter;
    public ClientHandler(Socket client, int clientID) {
        this.clientID = clientID;
        this.client = client;
        clientMessages = new LinkedBlockingDeque<>();
        clientReader = new ClientReader();
        clientReader.start();
    }

    public void setGameID( int gameID) {
        this.gameID = gameID;
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
            }
            finally {
                if (clientWriter != null)
                    clientWriter.interrupt();
                clientReader = null;
                Stop message = new Stop();
                message.put("handler", ClientHandler.this);
                message.apply();
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
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            } finally {
                clientWriter = null;
            }
        }
    }

    public void send(Message message) {
        clientMessages.add(message);
    }

    public void stop() {
        if (clientReader != null && clientReader.isAlive()) {
            clientReader.interrupt();
            clientReader = null;
        }
    }

    public boolean running() {
        return clientReader != null && clientReader.isAlive();
    }

    public int getClientID() {
        return clientID;
    }
}
