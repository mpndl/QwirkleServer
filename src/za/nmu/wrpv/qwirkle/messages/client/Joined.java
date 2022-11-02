package za.nmu.wrpv.qwirkle.messages.client;

import za.nmu.wrpv.qwirkle.messages.Message;

import java.io.Serial;
import java.io.Serializable;

public class Joined extends Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 102L;
}
