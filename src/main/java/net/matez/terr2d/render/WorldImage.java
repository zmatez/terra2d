package net.matez.terr2d.render;

import net.matez.terr2d.block.Block;
import net.matez.terr2d.block.BlockRegistry;
import net.matez.terr2d.math.BlockPos;
import net.matez.terr2d.math.ColumnPos;
import net.matez.terr2d.setup.Main;
import net.matez.terr2d.world.BlockColumn;
import net.matez.terr2d.world.World;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;

public class WorldImage {
    private BufferedImage bufferedImage;
    private static Color CAMERA_COLOR = new Color(123, 255, 72, 175);
    private BlockPos oldCameraPos;
    private int oldDetails = 0;
    private float oldZoom = 0;
    private int oldBlockSize = 0;
    private int[][] renderBlocks;
    public WorldImage(){

    }

    public void createBufferedImage(int width, int height){
        Main.LOGGER.debug("Creating buffered image with size: " + width + "x" + height);
        bufferedImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        renderBlocks = new int[width][height];
    }

    public void render(World world, Camera camera, boolean enableY, int details, float zoom, ImagePanel panel){
        if(bufferedImage==null){
            Main.LOGGER.fatal("Buffered image does not exist, cannot render!");
            return;
        }
        int screenWidth = bufferedImage.getWidth();
        int screenHeight = bufferedImage.getHeight();

        int blockSize = (int)Math.ceil((float)details / zoom);

        float visibleScreenWidth = screenWidth / zoom;
        float visibleScreenHeight = screenHeight / zoom;
        int worldWidth = (int)Math.ceil((float)visibleScreenWidth);
        int worldHeight = (int)Math.ceil((float)visibleScreenHeight);

        BlockPos cameraPos = camera.getLocation();
        if(oldCameraPos==null){
            oldCameraPos = cameraPos;
        }
        int cameraX = cameraPos.getX();
        int cameraZ = cameraPos.getZ();
        int moveX = oldCameraPos.getX()-cameraX;
        int moveZ = oldCameraPos.getZ()-cameraZ;

        int renderStartX = 0;
        int renderStartZ = 0;
        int renderEndX = screenWidth;
        int renderEndZ = screenHeight;

        int worldStartX = (int)Math.ceil((float)(cameraX - worldWidth/2) / zoom);
        int worldStartZ = (int)Math.ceil((float)(cameraZ - worldHeight/2) / zoom);
        int worldEndX = (int)Math.ceil((float)(cameraX + worldWidth/2) / zoom);
        int worldEndZ = (int)Math.ceil((float)(cameraZ + worldHeight/2) / zoom);

        refreshBlockSize(blockSize);
        move(moveX, moveZ);

        for(int x = renderStartX; x < renderEndX; x += blockSize){
            for(int z = renderStartZ; z < renderEndZ; z += blockSize){
                int blockX = worldStartX + x;
                int blockZ = worldStartZ + z;

                setBlock(world,blockX,blockZ,x,z,blockSize,details,zoom);
            }
        }

        //buff.refresh(world,camera,enableY,details,zoom,screenWidth,screenHeight);

        this.oldCameraPos = cameraPos;
        this.oldDetails = details;
        this.oldZoom = zoom;
        this.oldBlockSize = blockSize;

        panel.invalidate();
        panel.validate();
        panel.repaint();
    }

    private void setBlock(World world, int blockX, int blockZ, int x, int z, int blockSize, int details, float zoom){
        if(bufferedImage.getRGB(x,z)==0 || zoom!=oldZoom) {
            BlockColumn column = world.getColumn(new ColumnPos(blockX, blockZ));
            if (column != null && column.isDirty()) {
                renderBlock(x, column.getColumnMaxHeight(), z, blockSize, column.getBlock(column.getColumnMaxHeight()));
                return;
            }
        }
        int rgb = renderBlocks[x][z];
        renderBlock(x,z,blockSize,rgb);
    }

    private void move(int moveX, int moveZ){
        if(moveX!=0 || moveZ!=0){
            int w = getWorldWidth();
            int h = getWorldHeight();
            int[][] newRenderBlocks = new int[w][h];
            for(int x = 0; x < w; x++){
                for(int z = 0; z < h; z++){
                    int oldRgb = renderBlocks[x][z];
                    int newX = x + moveX;
                    int newZ = z + moveZ;
                    if(newX >= 0 && newZ >= 0 && newX < w && newZ < h){
                        newRenderBlocks[newX][newZ] = oldRgb;
                    }
                }
            }
            renderBlocks = newRenderBlocks;
        }
    }

    private void refreshBlockSize(int blockSize){
        if(blockSize != oldBlockSize && oldBlockSize!=0){
            int w = getWorldWidth();
            int h = getWorldHeight();
            int[][] newRenderBlocks = new int[w][h];
            for(int x = 0; x < w; x+=blockSize){
                for(int z = 0; z < h; z+=blockSize){
                    int oldRgb = renderBlocks[(int)Math.ceil((float)x/oldBlockSize)][(int)Math.ceil((float)z/oldBlockSize)];
                    for(int i = x; i < x + blockSize; i++){
                        for(int j = z; j < z + blockSize; j++){
                            if(i>=bufferedImage.getWidth()){
                                break;
                            }
                            if(j>=bufferedImage.getHeight()){
                                break;
                            }
                            try {
                                renderBlocks[i][j] = oldRgb;
                            }catch (ArrayIndexOutOfBoundsException e){
                                Main.LOGGER.fatal("Coordinate out of bounds: " + i + " " + j + " out of " + bufferedImage.getWidth() + " " + bufferedImage.getHeight());
                            }
                        }
                    }
                }
            }
            renderBlocks = newRenderBlocks;
        }
    }

    public int getWorldWidth(){
        return bufferedImage.getWidth();
    }

    public int getWorldHeight(){
        return bufferedImage.getHeight();
    }

    private void renderBlock(int x, int yFactor, int z, int size, Block block){
        if(x>bufferedImage.getWidth()){
            return;
        }
        if(z>bufferedImage.getHeight()){
            return;
        }
        if(x<0){
            return;
        }
        if(z<0){
            return;
        }

        Color color = block.getColor();
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int a = color.getAlpha();

        int darken = yFactor;
        r = r - darken;
        g = g - darken;
        b = b - darken;
        Color finalColor = new Color(r,g,b,a);

        for(int i = x; i < x + size; i++){
            for(int j = z; j < z + size; j++){
                if(i>=bufferedImage.getWidth()){
                    break;
                }
                if(j>=bufferedImage.getHeight()){
                    break;
                }
                try {
                    bufferedImage.setRGB(i, j, finalColor.getRGB());
                    renderBlocks[i][j] = finalColor.getRGB();
                }catch (ArrayIndexOutOfBoundsException e){
                    Main.LOGGER.fatal("Coordinate out of bounds: " + i + " " + j + " out of " + bufferedImage.getWidth() + " " + bufferedImage.getHeight());
                }
            }
        }
    }

    private void renderBlock(int x, int z, int size, int rgb){
        if(x>bufferedImage.getWidth()){
            return;
        }
        if(z>bufferedImage.getHeight()){
            return;
        }
        if(x<0){
            return;
        }
        if(z<0){
            return;
        }

        for(int i = x; i < x + size; i++){
            for(int j = z; j < z + size; j++){
                if(i>=bufferedImage.getWidth()){
                    break;
                }
                if(j>=bufferedImage.getHeight()){
                    break;
                }
                try {
                    bufferedImage.setRGB(i, j, rgb);
                }catch (ArrayIndexOutOfBoundsException e){
                    Main.LOGGER.fatal("Coordinate out of bounds: " + i + " " + j + " out of " + bufferedImage.getWidth() + " " + bufferedImage.getHeight());
                }
            }
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
