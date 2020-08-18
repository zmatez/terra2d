package net.matez.terr2d.render;

import net.matez.terr2d.setup.Main;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ScreenImage {
    private BufferedImage bufferedImage;
    public ScreenImage(){

    }

    public void createBufferedImage(int width, int height){
        Main.LOGGER.debug("Creating buffered image with size: " + width + "x" + height);
        bufferedImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
    }

    public boolean setColor(int x, int z, int rgb){
        if(bufferedImage.getRGB(x,z)==0){
            bufferedImage.setRGB(x,z,rgb);
            return true;
        }
        return false;
    }

    public void move(int moveX, int moveZ){
        if(moveX != 0){
            for(int i = 0; i < bufferedImage.getWidth(); i++){
                for(int j = 0; j < bufferedImage.getHeight(); j++){

                }
            }
        }
    }

}
