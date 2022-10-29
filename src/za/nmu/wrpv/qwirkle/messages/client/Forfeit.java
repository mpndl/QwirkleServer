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

        Game game = Server.getGame(handler.gameID);
        if(game != null && game.remove(handler.getClientID())) {
            Player player = game.model.getPlayer(handler.playerName);
            if (game.clientCount() == 0 || game.clientCount() == 1) {
                System.out.println(">>> GAME " + handler.gameID + " ENDED");
                remove("handler");
                PubSubBroker.publish(game.gameID, game.topic("stop"), this);
                game.removeAll();
                Server.removeGame(game.gameID);
            }else {
                System.out.println(">>> GAME " + handler.gameID + " FORFEITED -> clientID = " + handler.getClientID());
                Forfeit message = new Forfeit();
                message.put("player", player);
                PubSubBroker.publish(handler.getClientID(), game.topic("forfeit"), message);
            }
        }
    }
}
