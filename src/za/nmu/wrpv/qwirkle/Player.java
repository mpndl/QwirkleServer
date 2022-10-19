package za.nmu.wrpv.qwirkle;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Player implements Serializable {
    @Serial
    private static final long serialVersionUID = 30L;
    public List<Tile> tiles = new ArrayList<>();
    public Name name;
    public String color;
    public int points = 0;
    public enum Name {
        PLAYER1, PLAYER2, PLAYER3, PLAYER4;
    }
}
