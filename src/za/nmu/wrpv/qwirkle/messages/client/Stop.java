package za.nmu.wrpv.qwirkle.messages.client;

import za.nmu.wrpv.qwirkle.ClientHandler;
import za.nmu.wrpv.qwirkle.Game;
import za.nmu.wrpv.qwirkle.Server;
import za.nmu.wrpv.qwirkle.messages.Message;

import java.io.Serial;

public class Stop extends Message {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public void apply() {
        ClientHandler handler = (ClientHandler) get("handler");
        handler.stop();
        Server.game.remove(handler.getClientID());
    }
}
