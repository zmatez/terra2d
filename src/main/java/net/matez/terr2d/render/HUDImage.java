package net.matez.terr2d.render;

import net.matez.terr2d.math.BlockPos;
import net.matez.terr2d.render.ImagePanel;
import net.matez.terr2d.render.Camera;
import net.matez.terr2d.setup.Main;
import net.matez.terr2d.world.World;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

public class HUDImage {
    private BufferedImage bufferedImage;
    private static Color POINTER_COLOR = new Color(255, 140, 149, 175);

    private int oldPointerX, oldPointerY;

    public HUDImage() {

    }

    public void createBufferedImage(int width, int height) {
        Main.LOGGER.debug("Creating buffered image with size: " + width + "x" + height);
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    public void render(Camera camera, int details, float zoom, int mouseX, int mouseY, ImagePanel panel){
        if(bufferedImage==null){
            Main.LOGGER.fatal("Buffered image does not exist, cannot render!");
            return;
        }
        if(oldPointerX != mouseX || oldPointerY != mouseY) {
            Main.LOGGER.debug("f");
            int[] data = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();
            Arrays.fill(data, 0x00000000);//transparency

            oldPointerX = mouseX;
            oldPointerY = mouseY;

            int screenWidth = bufferedImage.getWidth();
            int screenHeight = bufferedImage.getHeight();

            int blockSize = (int) Math.ceil((float) details / zoom);

            BlockPos cameraPos = camera.getLocation();
            int cameraX = cameraPos.getX();
            int cameraZ = cameraPos.getZ();

            int renderStartX = 0;
            int renderStartZ = 0;
            int renderEndX = screenWidth;
            int renderEndZ = screenHeight;

            for (int x = renderStartX; x < renderEndX; x += blockSize) {
                for (int z = renderStartZ; z < renderEndZ; z += blockSize) {
                    if (x <= mouseX && x + blockSize > mouseX) {
                        if (z <= mouseY && z + blockSize > mouseY) {
                            renderBlock(x, z, blockSize, POINTER_COLOR);
                        }
                    }
                }
            }

            panel.invalidate();
            panel.validate();
            panel.repaint();
        }
    }

    private void renderBlock(int x, int z, int size, Color color) {
        if (x > bufferedImage.getWidth()) {
            Main.LOGGER.fatal("Rendering block: X" + x + ">" + bufferedImage.getWidth());
            return;
        }
        if (z > bufferedImage.getHeight()) {
            Main.LOGGER.fatal("Rendering block: Z" + z + ">" + bufferedImage.getHeight());
            return;
        }
        if (x < 0) {
            Main.LOGGER.fatal("Rendering block: X" + x + "<" + 0);
            return;
        }
        if (z < 0) {
            Main.LOGGER.fatal("Rendering block: Z" + z + "<" + 0);
            return;
        }


        for (int i = x; i < x + size; i++) {
            for (int j = z; j < z + size; j++) {
                if (i >= bufferedImage.getWidth()) {
                    break;
                }
                if (j >= bufferedImage.getHeight()) {
                    break;
                }
                try {
                    bufferedImage.setRGB(i, j, color.getRGB());
                } catch (ArrayIndexOutOfBoundsException e) {
                    Main.LOGGER.fatal("Coordinate out of bounds: " + i + " " + j + " out of " + bufferedImage.getWidth() + " " + bufferedImage.getHeight());
                }
            }
        }
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }
}
