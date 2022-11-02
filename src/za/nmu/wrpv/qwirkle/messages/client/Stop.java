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

        String playerName = handler.name;
        Game game = GamesHandler.getGame(handler.gameID);
        if(game != null) {
            if (game.began()) {
                Player player = game.model.getPlayer(playerName);
                if (game.clientCount() == 0) {
                    remove("handler");
                    System.out.println(">>>s GAME " + handler.gameID + " ENDED");
                    GamesHandler.removeGame(game.gameID);
                    PubSubBroker.publish(game.gameID, game.topic("stop"), this);
                }else {
                    if (player != null) {
                        // There's at least one player left, wait for other players to reconnect
                        if (player.name == game.model.currentPlayer.name) game.model.setNewCurrentPlayer(player);
                        System.out.println(">>> GAME " + handler.gameID + " FORFEITED -> clientID = " + handler.getClientID());
                        Forfeit message = new Forfeit();
                        message.put("player", player);
                        PubSubBroker.publish(handler.getClientID(), game.topic("forfeit"), message);
                    }
                }
            }else {
                boolean removed =  game.remove(handler.getClientID());
                if (removed) {
                    System.out.println(">>> GAME " + handler.gameID + " LEFT -> clientID = " + handler.getClientID());
                    if (game.ready()) {
                        Countdown msg = new Countdown();
                        msg.put("seconds", CountdownThread.getCurrentSeconds());
                        PubSubBroker.publish(game.gameID, game.topic("countdown"), msg);
                        GamesHandler.resetCountdown(handler, game);
                    } else {
                        Waiting msg = new Waiting();
                        PubSubBroker.publish(game.gameID, game.topic("wait"), msg);
                        GamesHandler.stopCountdown();
                    }
                }
            }
        }
    }
}
