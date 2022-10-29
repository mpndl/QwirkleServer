package za.nmu.wrpv.qwirkle.messages.client;

import za.nmu.wrpv.qwirkle.*;
import za.nmu.wrpv.qwirkle.messages.Message;

import java.io.Serial;

public class Stop extends Message {
    @Serial
    private static final long serialVersionUID = 1L;
    @Override
    public void apply() {
        ClientHandler handler = (ClientHandler) get("handler");
        handler.stop();

        String playerName = handler.playerName;
        Game game = Server.getGame(handler.gameID);
        if(game != null) {
            if (game.began) {
                Player player = game.model.getPlayer(playerName);
                if (game.clientCount() == 0) {
                    remove("handler");
                    System.out.println(">>> GAME " + handler.gameID + " ENDED");
                    game.removeAll();
                    Server.removeGame(game.gameID);
                }else {
                    System.out.println(">>> GAME " + handler.gameID + " FORFEITED -> clientID = " + handler.getClientID());
                    Forfeit message = new Forfeit();
                    message.put("player", player);
                    PubSubBroker.publish(handler.getClientID(), game.topic("forfeit"), message);
                }
            }else {
                if (game.ready()) {
                    System.out.println(">>> GAME " + handler.gameID + " LEFT -> clientID = " + handler.getClientID());
                    Countdown msg = new Countdown();
                    msg.put("seconds", Server.currentSeconds);
                    PubSubBroker.publish(game.gameID, game.topic("countdown"), msg);
                } else {
                    Waiting msg = new Waiting();
                    PubSubBroker.publish(game.gameID, game.topic("wait"), msg);
                    Server.countingDown = false;
                    Server.interrupt();
                }
                if (Server.pID > 0)
                    Server.pID--;
            }
        }
    }
}
