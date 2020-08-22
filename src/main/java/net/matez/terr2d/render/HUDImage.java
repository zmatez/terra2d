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
    private static Color CAMERA_COLOR = new Color(3, 255, 57, 175);
    private static Color ZERO_POS_COLOR = new Color(140, 242, 255, 90);
    private static Color TRANSPARENT = new Color(0, 0, 0, 0);

    private int oldMouseX, oldMouseY, oldPointerX, oldPointerY;
    private int oldCameraX, oldCameraY;

    private float oldZoom;

    private static int HUD_SIZE = 3;

    public void createBufferedImage(int width, int height) {
        Main.LOGGER.debug("Creating buffered image with size: " + width + "x" + height);
        bufferedImage = RenderUtils.createHardwareAcceleratedImage(width,height,true);
    }

    public void render(Camera camera, float zoom, int mouseX, int mouseY, boolean refresh) {
        if (bufferedImage == null) {
            Main.LOGGER.fatal("Buffered image does not exist, cannot render!");
            return;
        }
        BlockPos cameraPos = camera.getLocation();
        int cameraX = cameraPos.getX();
        int cameraZ = cameraPos.getZ();

        if (oldMouseX != mouseX || oldMouseY != mouseY || cameraX != oldCameraX || cameraZ != oldCameraY || refresh || zoom != oldZoom) {
            int[] data = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();
            Arrays.fill(data, 0x00000000);//transparency
        }
        oldMouseX = mouseX;
        oldMouseY = mouseY;
        oldCameraX = cameraX;
        oldCameraY = cameraZ;
        oldZoom = zoom;

        int screenWidth = bufferedImage.getWidth();
        int screenHeight = bufferedImage.getHeight();


        int renderStartX = 0;
        int renderStartZ = 0;
        int renderEndX = screenWidth;
        int renderEndZ = screenHeight;

        int worldStartX = (int) Math.ceil((float) (cameraX - screenWidth / 2));
        int worldStartZ = (int) Math.ceil((float) (cameraZ - screenHeight / 2));

        for (int x = renderStartX; x < renderEndX; x ++) {
            for (int z = renderStartZ; z < renderEndZ; z ++) {
                if ((x + worldStartX <= 0 && x + worldStartX + HUD_SIZE > 0) || (z + worldStartZ <= 0 && z + worldStartZ + HUD_SIZE > 0)) {
                    renderBlock(x, z, HUD_SIZE, ZERO_POS_COLOR);
                }

                if (x/zoom <= mouseX && x/zoom + HUD_SIZE > mouseX) {
                    if (z/zoom <= mouseY && z/zoom + HUD_SIZE > mouseY) {
                        oldPointerX = x;
                        oldPointerY = z;
                        renderBlock(x, z, HUD_SIZE, POINTER_COLOR);
                    }
                }
                if (x + worldStartX <= cameraX && x + worldStartX + HUD_SIZE > cameraX) {
                    if (z + worldStartZ <= cameraZ && z + worldStartZ + HUD_SIZE > cameraZ) {
                        renderBlock(x, z, HUD_SIZE, CAMERA_COLOR);
                    }
                }

            }
        }
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
