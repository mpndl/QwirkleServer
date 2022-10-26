package za.nmu.wrpv.qwirkle.messages.client;

import za.nmu.wrpv.qwirkle.*;
import za.nmu.wrpv.qwirkle.messages.Message;

import java.io.Serial;
import java.util.Map;

public class Stop extends Message {
    @Serial
    private static final long serialVersionUID = 1L;
    @Override
    public void apply() {
        ClientHandler handler = (ClientHandler) get("handler");
        handler.stop();

        String playerName = handler.playerName;
        Game game = Server.getGame(handler.gameID);
        if(game != null && game.remove(handler.getClientID())) {
            Player player = (Player) get("player");
            if (game.model != null && player == null)
                player = game.model.getPlayer(playerName);
            if (!Server.countingDown)
                System.out.println(">>> GAME " + handler.gameID + " FORFEITED -> clientID = " + handler.getClientID());
            else System.out.println(">>> GAME " + handler.gameID + " LEFT -> clientID = " + handler.getClientID());

            if (game.ready() && !Server.countingDown) {
                if (game.clientCount() >= 2) {
                    Forfeit message = new Forfeit();
                    message.put("player", player);
                    PubSubBroker.publish(handler.getClientID(), "forfeit", message);
                } else {
                    data.remove("handler");
                    PubSubBroker.publish(handler.getClientID(), "stop", this);
                }
            }

            if (!game.began && game.clientCount() == 0) {
                System.out.println("------------------- GAME ENDED !game.began && game.playerCount() == 0");
                System.out.println(">>> GAME " + handler.gameID + " ENDED");
                remove("handler");
                PubSubBroker.publish(game.gameID, "stop", this);
                game.removeAll();
                Server.removeGame(game.gameID);
            }

            if (game.began && game.clientCount() < 2) {
                System.out.println("------------------ GAME ENDED game.began && game.playerCount() < 2");
                System.out.println(">>> GAME " + handler.gameID + " ENDED");
                remove("handler");
                PubSubBroker.publish(game.gameID, "stop", this);
                game.removeAll();
                Server.removeGame(game.gameID);
            }

            if (Server.pID > 0)
                Server.pID--;

            if (Server.countingDown) {
                if (game.ready()) {
                    Countdown msg = new Countdown();
                    msg.put("seconds", Server.currentSeconds);
                    PubSubBroker.publish(game.gameID, "countdown", msg);
                } else {
                    Waiting msg = new Waiting();
                    PubSubBroker.publish(game.gameID, "wait", msg);
                    Server.countingDown = false;
                    Server.interrupt();
                }
            }
        }
    }
}
