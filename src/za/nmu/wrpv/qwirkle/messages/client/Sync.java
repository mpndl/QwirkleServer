package za.nmu.wrpv.qwirkle.messages.client;

import java.io.Serial;
import java.io.Serializable;

import za.nmu.wrpv.qwirkle.ClientHandler;
import za.nmu.wrpv.qwirkle.Game;
import za.nmu.wrpv.qwirkle.GamesHandler;
import za.nmu.wrpv.qwirkle.PubSubBroker;
import za.nmu.wrpv.qwirkle.messages.Message;

public class Sync extends Message implements Serializable {
    @Serial
    private final static long serialVersionUID = 200L;

    @Override
    public void apply() {
        ClientHandler handler = (ClientHandler) get("handler");
        Game game = GamesHandler.getGame(handler.gameID);
        if(game != null) {
            if(game.model != null) {
                System.out.println(">>> ATTEMPTING SYNC gameID = " + game.gameID + ", clientID = " + handler.clientID);
                remove("handler");
                remove("currentPlayerIndex");
                put("currentPlayerIndex", game.model.getPlayerIndex(game.model.currentPlayer));
                put("currentPlayerName", game.model.currentPlayer.name);
                put("players", game.model.players);
                PubSubBroker.publish(game.gameID, game.topic("sync"), this);
            }
        }
    }
}
