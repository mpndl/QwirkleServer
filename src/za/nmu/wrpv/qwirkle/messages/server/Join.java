package za.nmu.wrpv.qwirkle.messages.server;

import za.nmu.wrpv.qwirkle.Server;
import za.nmu.wrpv.qwirkle.messages.Message;

import java.io.Serial;
import java.io.Serializable;

public class Join extends Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 100L;

    @Override
    public void apply() {
        int clientID = (int) get("clientID");
        int prevClientID = (int) get("prevClientID");
        Server.join(clientID,prevClientID);
    }
}
