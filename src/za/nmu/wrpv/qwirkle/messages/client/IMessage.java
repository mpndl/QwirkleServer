package za.nmu.wrpv.qwirkle.messages.client;

import za.nmu.wrpv.qwirkle.*;
import za.nmu.wrpv.qwirkle.messages.Message;

import java.io.Serial;
import java.io.Serializable;

public class IMessage extends Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 5L;

    @Override
    public void apply() {
        PlayerMessage message = (PlayerMessage)data.get("message");
        ClientHandler handler = (ClientHandler)data.get("handler");

        Message msg = new IMessage();
        msg.put("message", message);

        Game game = GamesHandler.getGame(handler.gameID);

        if (game != null) {
            game.model.messages.add(message);
            PubSubBroker.publish(handler.getClientID() + "", game.topic("messages"), msg);
        }
    }
}
