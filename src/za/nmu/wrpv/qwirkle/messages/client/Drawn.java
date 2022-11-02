package za.nmu.wrpv.qwirkle.messages.client;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import za.nmu.wrpv.qwirkle.*;
import za.nmu.wrpv.qwirkle.messages.Message;

public class Drawn extends Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 70L;

    @Override
    public void apply() {
        ClientHandler handler = (ClientHandler) data.get("handler");
        data.remove("handler");

        Game game = GamesHandler.getGame(handler.gameID);
        if (game != null) {
            Player player = (Player) data.get("player");
            List<Tile> bag = (List<Tile>) data.get("bag");
            game.model.updatePlayerTiles(player);
            game.model.bag = bag;
            game.model.turn();

            PubSubBroker.publish(handler.getClientID() + "", game.topic("drawn"), this);
        }
    }
}
