package za.nmu.wrpv.qwirkle.messages.client;

import za.nmu.wrpv.qwirkle.*;
import za.nmu.wrpv.qwirkle.messages.Message;

import java.io.Serial;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Stop extends Message {
    @Serial
    private static final long serialVersionUID = 1L;
    private final Lock lock = new ReentrantLock();
    @Override
    public void apply() {
        ClientHandler handler = (ClientHandler) get("handler");
        handler.stop();
        System.out.println(">>> GAME " + handler.gameID + " FORFEITED -> clientID = " + handler.getClientID());

        Player player = (Player) get("player");

        Game game = Server.getGame(handler.gameID);
        game.remove(handler.getClientID());


        if (game.ready()) {
            if (game.playerCount() >= 2) {
                Forfeit message = new Forfeit();
                message.put("player", player);
                PubSubBroker.publish(handler.getClientID(), "forfeit", message);
            } else {
                data.remove("handler");

                PubSubBroker.publish(handler.getClientID(), "stop", this);

                game.removeAll();
                Server.removeGame(game.gameID);

                System.out.println(">>> GAME " + handler.gameID + " ENDED");
            }
        }

        if (lock != null)
            lock.lock();
        if (Server.pID > 0)
            Server.pID--;

        if (Server.ready) {
            Waiting msg = new Waiting();
            msg.put("restart", 1);
            PubSubBroker.publish(game.gameID, "restart", msg);
            if (!game.ready())
                Server.ready = false;
        }
        if (lock != null)
            lock.unlock();

        PubSubBroker.unsubscribe(handler.subscriber);
    }
}
