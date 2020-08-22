package net.matez.terr2d.render;

import net.matez.terr2d.setup.Main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.util.Arrays;

public class DataImage {
    private int width, height;
    private boolean isLogoDrawn = false;
    private BufferedImage bufferedImage;
    private BufferedImage logo;
    private Graphics2D graphics;
    private int fontSize = 18;
    private boolean invertColors = false;
    private String oldText = "";
    private Font f = new Font("Consolas", Font.PLAIN, fontSize);
    private Font shadowFont = new Font("Consolas", Font.BOLD, fontSize);
    private int shadowRes = 2;
    private Color LIGHT = new Color(255, 255, 255, 230), DARK = new Color(0, 0, 0), BACKGROUND = new Color(0, 0, 0, 170);

    public void createBufferedImage(int width, int height) {
        Main.LOGGER.debug("Creating buffered image with size: " + width + "x" + height);
        bufferedImage = RenderUtils.createHardwareAcceleratedImage(width,height,true);
        graphics = (Graphics2D)bufferedImage.getGraphics();
        this.width=width;
        this.height=height;
        try {
            logo = ImageIO.read(DataImage.class.getResourceAsStream("/logo.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void render(String text, boolean refresh, boolean showBlackPanel, int fontSize) {
        if (oldText.equals(text) && !refresh) {
            return;
        }
        if(refresh){
            isLogoDrawn = false;
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
        String[] toDraw = text.split("\n");
        int startY = f.getSize();
        int enterPx = startY + 5;
        int currentLine = 0;
        for (String s : toDraw) {
            if (!invertColors) {
                graphics.setFont(shadowFont);
                graphics.setColor(DARK);
                graphics.drawString(s, shadowRes + 5, currentLine * enterPx + shadowRes + startY);
                graphics.setFont(f);
                graphics.setColor(LIGHT);
                graphics.drawString(s, 5, currentLine * enterPx + startY);
            } else {
                graphics.setFont(shadowFont);
                graphics.setColor(DARK);
                graphics.drawString(s, shadowRes + 5, currentLine * enterPx + shadowRes + startY);
                graphics.setFont(f);
                graphics.setColor(LIGHT);
                graphics.drawString(s, 5, currentLine * enterPx + startY);
            }
            currentLine++;
        }

        int finalLogoWidth = (logo.getWidth()-210)/20;
        int finalLogoHeight = (logo.getHeight()-41)/20;
        graphics.drawImage(logo, 0, height - (int)(finalLogoHeight*1.75),finalLogoWidth,finalLogoHeight, null);
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }
}

   
    
    
    