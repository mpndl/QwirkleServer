package za.nmu.wrpv.qwirkle.messages.client;

import za.nmu.wrpv.qwirkle.*;
import za.nmu.wrpv.qwirkle.messages.Message;

import java.io.Serial;
import java.io.Serializable;

public class Forfeit extends Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 80L;

    @Override
    public void apply() {
        ClientHandler handler = (ClientHandler) get("handler");
        handler.stop();

        Game game = GamesHandler.getGame(handler.gameID);
        if(game != null && game.remove(handler.getClientID())) {
            Player player = game.model.getPlayer(handler.name);

            game.model.removePlayer(player);

            // Player cannot rejoin when explicitly forfeited game
            if (game.handlers.size() < 2) {
                System.out.println(">>>f GAME " + handler.gameID + " ENDED");
                remove("handler");
                PubSubBroker.publish(game.gameID, game.topic("stop"), new Stop());
                GamesHandler.removeGame(game.gameID);
            }else {
                if (player.name == game.model.currentPlayer.name) game.model.setNewCurrentPlayer(player);

                System.out.println(">>> GAME " + handler.gameID + " FORFEITED -> clientID = " + handler.getClientID());
                Forfeit message = new Forfeit();
                message.put("player", player);
                message.put("currentPlayerIndex", game.model.getPlayerIndex(game.model.currentPlayer));
                PubSubBroker.publish(handler.getClientID(), game.topic("forfeit"), message);
            }
        }
    }
}
