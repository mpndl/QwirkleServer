package za.nmu.wrpv.qwirkle.messages.server;

import za.nmu.wrpv.qwirkle.ClientHandler;
import za.nmu.wrpv.qwirkle.Game;
import za.nmu.wrpv.qwirkle.GamesHandler;
import za.nmu.wrpv.qwirkle.Server;
import za.nmu.wrpv.qwirkle.messages.Message;
import za.nmu.wrpv.qwirkle.messages.client.ConnectionError;

import java.io.Serial;
import java.io.Serializable;

public class Rejoin extends Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 101L;

    @Override
    public void apply() {
        ClientHandler handler = (ClientHandler) get("handler");
        int clientID = (int) get("clientID");
        int gameID = (int) get("gameID");
        if (gameID != GamesHandler.gameID) {
            Game game = GamesHandler.getGame(gameID);
            if (game != null) {
                if (handler.connectErrCount == 0) {
                    game.rejoin(clientID, handler);
                }else {
                    handler.send(new ConnectionError());
                }
            }
        }
    }
}
