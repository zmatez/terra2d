package net.matez.terr2d.render;

import com.sun.istack.internal.Nullable;
import net.matez.terr2d.setup.Main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

public class DataImage {
    private BufferedImage bufferedImage;
    private int fontSize = 18;
    private boolean invertColors = false;
    private String oldText = "";
    private Font f = new Font("Consolas", Font.PLAIN, fontSize);
    private Font shadowFont = new Font("Consolas", Font.BOLD, fontSize);
    private int shadowRes = 2;
    private Color LIGHT = new Color(255, 255, 255, 230), DARK = new Color(0, 0, 0), BACKGROUND = new Color(0, 0, 0, 170);

    public void createBufferedImage(int width, int height) {
        Main.LOGGER.debug("Creating buffered image with size: " + width + "x" + height);
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    public void render(String text, @Nullable AdvancedImagePanel panel, boolean refresh, boolean showBlackPanel, int fontSize) {
        if (oldText.equals(text) || refresh) {
            return;
        }
        if(this.fontSize!=fontSize){
            this.fontSize = fontSize;
            f = new Font("Consolas", Font.PLAIN, fontSize);
            shadowFont = new Font("Consolas", Font.BOLD, fontSize);
        }

        oldText = text;
        int[] data = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();
        Arrays.fill(data, 0x00000000);//transparency

        if (showBlackPanel) {
            for (int i = 0; i < bufferedImage.getWidth() / 5; i++) {
                for (int j = 0; j < bufferedImage.getHeight(); j++) {
                    bufferedImage.setRGB(i, j, BACKGROUND.getRGB());
                }
            }
        }
        Graphics g = bufferedImage.getGraphics();

        String[] toDraw = text.split("\n");
        int startY = f.getSize();
        int enterPx = startY + 5;
        int currentLine = 0;
        for (String s : toDraw) {
            if (!invertColors) {
                g.setFont(shadowFont);
                g.setColor(DARK);
                g.drawString(s, shadowRes + 5, currentLine * enterPx + shadowRes + startY);
                g.setFont(f);
                g.setColor(LIGHT);
                g.drawString(s, 5, currentLine * enterPx + startY);
            } else {
                g.setFont(shadowFont);
                g.setColor(DARK);
                g.drawString(s, shadowRes + 5, currentLine * enterPx + shadowRes + startY);
                g.setFont(f);
                g.setColor(LIGHT);
                g.drawString(s, 5, currentLine * enterPx + startY);
            }
            currentLine++;
        }
        if (panel != null) {
            panel.invalidate();
            panel.validate();
            panel.repaint();
        }
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }
}

   
    
    
    