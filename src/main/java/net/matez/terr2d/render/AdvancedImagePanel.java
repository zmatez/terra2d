package net.matez.terr2d.render;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class AdvancedImagePanel extends JPanel {

    private BufferedImage image;
    private BufferedImage hud;
    private BufferedImage data;

    public AdvancedImagePanel(BufferedImage image, BufferedImage hud, BufferedImage data) {
        this.image = image;
        this.hud=hud;
        this.data=data;
    }

    public void setImage(BufferedImage image, BufferedImage hud, BufferedImage data) {
        this.image = image;
        this.hud=hud;
        this.data=data;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);
        g.drawImage(hud, 0, 0, null);
        g.drawImage(data, 0, 0, null);
    }

}