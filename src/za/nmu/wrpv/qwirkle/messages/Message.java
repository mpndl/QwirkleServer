package za.nmu.wrpv.qwirkle.messages;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 0L;
    private final Map<String, Object> data = new HashMap<>();
    public void apply() {}
    public void put(String key, Object value) {
        data.put(key, value);
    }
}
