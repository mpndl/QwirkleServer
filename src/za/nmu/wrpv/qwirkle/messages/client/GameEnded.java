package za.nmu.wrpv.qwirkle.messages.client;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import za.nmu.wrpv.qwirkle.*;
import za.nmu.wrpv.qwirkle.messages.Message;

public class GameEnded extends Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 75L;

    @Override
    public void apply() {
        ClientHandler handler = (ClientHandler) get("handler");
        remove("handler");
        Game game = GamesHandler.getGame(handler.gameID);
        if (game != null) {
            GamesHandler.removeGame(game.gameID);
            PubSubBroker.publish(handler.gameID, game.topic("ended"), this);
        }
    }
}
