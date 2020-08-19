package net.matez.terr2d.render;

import net.matez.terr2d.math.BlockPos;
import net.matez.terr2d.setup.Main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

public class HUDImage {
    private BufferedImage bufferedImage;
    private static Color POINTER_COLOR = new Color(255, 140, 149, 175);
    private static Color ZERO_POS_COLOR = new Color(140, 242, 255, 90);
    private static Color TRANSPARENT = new Color(0, 0, 0, 0);

    private int oldMouseX, oldMouseY, oldPointerX, oldPointerY, oldDetails;
    private float oldZoom;
    private int oldCameraX, oldCameraY;

    public void createBufferedImage(int width, int height) {
        Main.LOGGER.debug("Creating buffered image with size: " + width + "x" + height);
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    public void render(WorldImage image, Camera camera, int details, float zoom, int mouseX, int mouseY, AdvancedImagePanel panel, boolean refresh) {
        if (bufferedImage == null) {
            Main.LOGGER.fatal("Buffered image does not exist, cannot render!");
            return;
        }
        BlockPos cameraPos = camera.getLocation();
        int cameraX = cameraPos.getX();
        int cameraZ = cameraPos.getZ();

        if (oldMouseX != mouseX || oldMouseY != mouseY || cameraX != oldCameraX || cameraZ != oldCameraY || zoom != oldZoom || details != oldDetails || refresh) {
            int[] data = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();
            Arrays.fill(data, 0x00000000);//transparency
        }
        oldMouseX = mouseX;
        oldMouseY = mouseY;
        oldCameraX = cameraX;
        oldCameraY = cameraZ;
        oldDetails = details;
        oldZoom = zoom;

        int screenWidth = bufferedImage.getWidth();
        int screenHeight = bufferedImage.getHeight();

        int blockSize = (int) Math.ceil((float) details / zoom);


        int renderStartX = 0;
        int renderStartZ = 0;
        int renderEndX = screenWidth;
        int renderEndZ = screenHeight;

        float visibleScreenWidth = screenWidth / zoom;
        float visibleScreenHeight = screenHeight / zoom;
        int worldWidth = (int) Math.ceil((float) visibleScreenWidth);
        int worldHeight = (int) Math.ceil((float) visibleScreenHeight);
        ;
        int worldStartX = (int) Math.ceil((float) (cameraX - worldWidth / 2) / zoom);
        int worldStartZ = (int) Math.ceil((float) (cameraZ - worldHeight / 2) / zoom);

        for (int x = renderStartX; x < renderEndX; x += blockSize) {
            for (int z = renderStartZ; z < renderEndZ; z += blockSize) {
                if ((x + worldStartX <= 0 && x + worldStartX + blockSize > 0) || (z + worldStartZ <= 0 && z + worldStartZ + blockSize > 0)) {
                    renderBlock(x, z, blockSize, ZERO_POS_COLOR);
                }

                if (x <= mouseX && x + blockSize > mouseX) {
                    if (z <= mouseY && z + blockSize > mouseY) {
                        oldPointerX = x;
                        oldPointerY = z;
                        renderBlock(x, z, blockSize, POINTER_COLOR);
                    }
                }

            }
        }

        panel.invalidate();
        panel.validate();
        panel.repaint();


        //renderBlock(cameraX, cameraZ, blockSize, POINTER_COLOR);
    }

    public int getOldPointerX() {
        return oldPointerX;
    }

    public int getOldPointerY() {
        return oldPointerY;
    }

    private void renderBlock(int x, int z, int size, Color color) {
        renderBlock(x, z, size, color.getRGB());
    }

    private void renderBlock(int x, int z, int size, int rgb) {
        if (x > bufferedImage.getWidth()) {
            return;
        }
        if (z > bufferedImage.getHeight()) {
            return;
        }
        if (x < 0) {
            return;
        }
        if (z < 0) {
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
                    //Main.LOGGER.debug("rendering pointer " + color.toString());
                    bufferedImage.setRGB(i, j, rgb);
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
