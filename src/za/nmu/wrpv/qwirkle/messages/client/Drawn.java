package za.nmu.wrpv.qwirkle.messages.client;

import java.io.Serial;
import java.io.Serializable;

import za.nmu.wrpv.qwirkle.ClientHandler;
import za.nmu.wrpv.qwirkle.PubSubBroker;
import za.nmu.wrpv.qwirkle.messages.Message;

public class Drawn extends Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 70L;

    @Override
    public void apply() {
        ClientHandler handler = (ClientHandler) data.get("handler");
        data.remove("handler");
        PubSubBroker.publish(handler.getClientID() + "", "drawn", this);
    }
}
