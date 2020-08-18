package net.matez.terr2d.block;

import java.awt.*;

public class Block {
    private Color color;
    private String name;
    public Block(Color color, String name){
        this.color=color;
        this.name=name;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }
}
