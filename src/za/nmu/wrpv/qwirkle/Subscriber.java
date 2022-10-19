package za.nmu.wrpv.qwirkle;

import java.util.Map;

@FunctionalInterface
public interface Subscriber {
    void onPublished(Object publisher, String topic, Map<String,Object> params);
}
