package net.matez.terr2d.render;

import com.sun.istack.internal.Nullable;
import net.matez.terr2d.block.Block;
import net.matez.terr2d.block.BlockRegistry;
import net.matez.terr2d.math.BlockPos;
import net.matez.terr2d.math.ColumnPos;
import net.matez.terr2d.setup.Main;
import net.matez.terr2d.world.BlockColumn;
import net.matez.terr2d.world.World;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class WorldImage {
    private BufferedImage bufferedImage;
    private static Color CAMERA_COLOR = new Color(123, 255, 72, 175);
    private BlockPos oldCameraPos;
    private int oldDetails = 0;
    private float oldZoom = 0;
    private int oldBlockSize = 0;
    private int[][][] renderBlocks;

    public void createBufferedImage(int width, int height){
        Main.LOGGER.debug("Creating buffered image with size: " + width + "x" + height);
        bufferedImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        renderBlocks = new int[width][height][2];
    }

    public void render(World world, Camera camera, boolean enableY, int details, float zoom, @Nullable AdvancedImagePanel panel, boolean refresh){
        if(bufferedImage==null){
            Main.LOGGER.fatal("Buffered image does not exist, cannot render!");
            return;
        }
        if(refresh){
            renderBlocks = new int[getWorldWidth()][getWorldHeight()][2];
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

        refreshBlockSize(blockSize);
        move(moveX, moveZ);

        for(int x = renderStartX; x < renderEndX; x += blockSize){
            for(int z = renderStartZ; z < renderEndZ; z += blockSize){
                int blockX = worldStartX + x;
                int blockZ = worldStartZ + z;

                setBlock(world,blockX,blockZ,x,z,blockSize,details,zoom);
            }
        }

        this.oldCameraPos = cameraPos;
        this.oldDetails = details;
        this.oldZoom = zoom;
        this.oldBlockSize = blockSize;

        if(panel!=null) {
            panel.invalidate();
            panel.validate();
            panel.repaint();
        }
    }

    public int getYAt(int x, int z){
        if(x>=0 && z>=0 && x<getWorldWidth() && z<getWorldHeight()) {
            return renderBlocks[x][z][1];
        }
        return -1;
    }

    private void setBlock(World world, int blockX, int blockZ, int x, int z, int blockSize, int details, float zoom){
        if(getRGB(x,z)==0 || zoom!=oldZoom) {
            BlockColumn column = world.getColumn(new ColumnPos(blockX, blockZ));
            if (column != null && column.isDirty()) {
                if(column.getColumnMaxHeight()<63 && Main.shouldShowWater()) {
                    renderBlock(x, column.getColumnMaxHeight(), z, blockSize, BlockRegistry.WATER);
                }else{
                    renderBlock(x, column.getColumnMaxHeight(), z, blockSize, column.getBlock(column.getColumnMaxHeight()));
                }
                return;
            }
        }
        if(!isOutOfBounds(x,z)) {
            try {
                int rgb = renderBlocks[x][z][0];
                renderBlock(x, z, blockSize, rgb);
            }catch (ArrayIndexOutOfBoundsException e){

            }
        }
    }

    private void move(int moveX, int moveZ){
        if(moveX!=0 || moveZ!=0){
            int w = getWorldWidth();
            int h = getWorldHeight();
            int[][][] newRenderBlocks = new int[w][h][2];
            for(int x = 0; x < w; x++){
                for(int z = 0; z < h; z++){
                    int oldRgb = renderBlocks[x][z][0];
                    int oldY = renderBlocks[x][z][1];
                    int newX = x + moveX;
                    int newZ = z + moveZ;
                    if(newX >= 0 && newZ >= 0 && newX < w && newZ < h){
                        newRenderBlocks[newX][newZ][0] = oldRgb;
                        newRenderBlocks[newX][newZ][1] = oldY;
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
            int[][][] newRenderBlocks = new int[w][h][2];
            for(int x = 0; x < w; x+=blockSize){
                for(int z = 0; z < h; z+=blockSize){
                    int[] buff = renderBlocks[(int)Math.ceil((float)x/oldBlockSize)][(int)Math.ceil((float)z/oldBlockSize)];
                    int oldRgb = buff[0];
                    int oldY = buff[1];
                    for(int i = x; i < x + blockSize; i++){
                        for(int j = z; j < z + blockSize; j++){
                            if(i>=bufferedImage.getWidth()){
                                break;
                            }
                            if(j>=bufferedImage.getHeight()){
                                break;
                            }
                            try {
                                renderBlocks[i][j][0] = oldRgb;
                                renderBlocks[i][j][1] = oldY;
                            }catch (ArrayIndexOutOfBoundsException e){

                            }
                        }
                    }
                }
            }
            renderBlocks = newRenderBlocks;
        }
    }

    public int getOldBlockSize() {
        return oldBlockSize;
    }

    public int getWorldWidth(){
        return bufferedImage.getWidth();
    }

    public int getWorldHeight(){
        return bufferedImage.getHeight();
    }

    private void renderBlock(int x, int y, int z, int size, Block block){
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

        int darken = 0;
        if(Main.getHeightDiv()==0){
            darken = y;
        }else{
            darken = ((int)(y/Main.getHeightDiv()))*Main.getHeightDiv();
        }
        r = r - darken;
        g = g - darken;
        b = b - darken;
        if(r > 255){
            r = 255;
        }
        if(g > 255){
            g = 255;
        }
        if(b > 255){
            b = 255;
        }
        if(r < 0){
            r = 0;
        }
        if(g < 0){
            g = 0;
        }
        if(b < 0){
            b = 0;
        }
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
                    renderBlocks[i][j][0] = finalColor.getRGB();
                    renderBlocks[i][j][1] = y;
                }catch (ArrayIndexOutOfBoundsException e){

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

                }
            }
        }
    }

    private int getRGB(int x, int z) {
        if (x > bufferedImage.getWidth()) {
            return 0;
        }
        if (z > bufferedImage.getHeight()) {
            return 0;
        }
        if (x < 0) {
            return 0;
        }
        if (z < 0) {
            return 0;
        }
        return bufferedImage.getRGB(x,z);
    }

    private boolean isOutOfBounds(int x, int z) {
        if (x > bufferedImage.getWidth()) {
            return true;
        }
        if (z > bufferedImage.getHeight()) {
            return true;
        }
        if (x < 0) {
            return true;
        }
        if (z < 0) {
            return true;
        }
        return false;
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }
}
