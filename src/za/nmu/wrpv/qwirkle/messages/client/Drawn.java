package za.nmu.wrpv.qwirkle.messages.client;

import java.io.Serial;
import java.io.Serializable;

import za.nmu.wrpv.qwirkle.ClientHandler;
import za.nmu.wrpv.qwirkle.Game;
import za.nmu.wrpv.qwirkle.PubSubBroker;
import za.nmu.wrpv.qwirkle.Server;
import za.nmu.wrpv.qwirkle.messages.Message;

public class Drawn extends Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 70L;

    @Override
    public void apply() {
        ClientHandler handler = (ClientHandler) data.get("handler");
        data.remove("handler");
        Game game = Server.getGame(handler.gameID);
        if (game != null)
            PubSubBroker.publish(handler.getClientID() + "", game.topic("drawn"), this);
    }
}
