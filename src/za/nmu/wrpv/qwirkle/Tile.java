package za.nmu.wrpv.qwirkle;

import java.io.Serializable;

public class Tile implements Serializable {
    public int xPos;
    public int yPos;
    public Shape shape;
    public Color color;

    public enum Shape{
        CLOVER, FPSTAR, EPSTAR, SQUARE, CIRCLE, DIAMOND;
    }

    public enum Color {
        RED, ORANGE, YELLOW, GREEN, BLUE, PURPLE;
    }

    @Override
    public String toString() {
        if (color != null)
            return color.toString().toLowerCase() + "_" + shape.toString().toLowerCase();
        else return "blank";
    }
}
