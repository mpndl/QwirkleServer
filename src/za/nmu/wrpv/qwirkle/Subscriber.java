package za.nmu.wrpv.qwirkle;

import za.nmu.wrpv.qwirkle.messages.Message;

@FunctionalInterface
public interface Subscriber {
    void onPublished(Object publisher, String topic, Object data);
}
