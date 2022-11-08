package za.nmu.wrpv.qwirkle.messages.client;

import java.io.Serial;
import java.io.Serializable;

import za.nmu.wrpv.qwirkle.ClientHandler;
import za.nmu.wrpv.qwirkle.Game;
import za.nmu.wrpv.qwirkle.GamesHandler;
import za.nmu.wrpv.qwirkle.messages.Message;

public class ConnectionError extends Message implements Serializable {
    @Serial
    private final static long serialVersionUID = 300L;

    @Override
    public void apply() {
        ClientHandler handler = (ClientHandler) get("handler");
        int connectErrCount = (int) get("connectErrCount");
        int gameID = (int) get("gameID");
        if (gameID != GamesHandler.gameID) {
            Game game = GamesHandler.getGame(gameID);
            if (game != null) {
                handler.connectErrCount = connectErrCount;
            }
        }
    }
}
