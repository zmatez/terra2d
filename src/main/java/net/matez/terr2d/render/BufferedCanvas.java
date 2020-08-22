package net.matez.terr2d.render;

import javafx.scene.canvas.Canvas;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class BufferedCanvas extends Canvas {

    private BufferedImage world;
    private BufferedImage hud;
    private BufferedImage data;
    private int width, height;

    public BufferedCanvas(BufferedImage image, BufferedImage hud, BufferedImage data, int width, int height) {
        this.world = image;
        this.hud=hud;
        this.data=data;
        this.width=width;
        this.height=height;
    }

    public void setImage(BufferedImage image, BufferedImage hud, BufferedImage data, int width, int height) {
        this.world = image;
        this.hud=hud;
        this.data=data;
        this.width=width;
        this.height=height;
    }


}