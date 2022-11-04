package za.nmu.wrpv.qwirkle.messages.client;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import za.nmu.wrpv.qwirkle.*;
import za.nmu.wrpv.qwirkle.messages.Message;

public class Played extends Message implements Serializable {
    @Serial
    private final static long serialVersionUID = 50L;

    @Override
    public void apply() {
        ClientHandler handler = (ClientHandler) data.get("handler");
        data.remove("handler");
        Game game = GamesHandler.getGame(handler.gameID);
        if (game != null) {
            Player player = (Player) data.get("player");
            List<Tile> bag = (List<Tile>) data.get("bag");
            Tile[][] board = (Tile[][]) data.get("board");
            List<Tile> places = (ArrayList<Tile>)data.get("places");

            game.model.updatePlayerTiles(player);
            game.model.updatePlayerScore(player);
            game.model.board = board;
            game.model.bag = bag;
            game.model.placed.addAll(places);

            game.model.turn();

            put("currentPlayerIndex", game.model.getPlayerIndex(game.model.currentPlayer));

            PubSubBroker.publish(handler.getClientID() + "", game.topic("played"), this);
        }
    }
}
