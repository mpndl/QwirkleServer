package za.nmu.wrpv.qwirkle.messages.server;

import za.nmu.wrpv.qwirkle.ClientHandler;
import za.nmu.wrpv.qwirkle.messages.Message;

import java.io.Serial;

public class Stop extends Message {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public void apply() {
        ((ClientHandler) get("handler")).stop();
    }
}
