package za.nmu.wrpv.qwirkle.messages.server;

import za.nmu.wrpv.qwirkle.ClientHandler;
import za.nmu.wrpv.qwirkle.Game;
import za.nmu.wrpv.qwirkle.GamesHandler;
import za.nmu.wrpv.qwirkle.Server;
import za.nmu.wrpv.qwirkle.messages.Message;

import java.io.Serial;
import java.io.Serializable;

public class Join extends Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 100L;

    @Override
    public void apply() {
        ClientHandler handler = (ClientHandler) get("handler");
        int clientID = (int) get("clientID");
        int prevClientID = (int) get("prevClientID");
        int gameID = (int) get("gameID");
        handler.prevClientID = prevClientID;
        Game game = GamesHandler.getGame(gameID);
        if (game != null) {
            if (game.rejoin(clientID, handler)) return;
            if (game.rejoin(prevClientID, handler)) return;
        }
        Server.join(clientID,prevClientID, handler);
    }
}
