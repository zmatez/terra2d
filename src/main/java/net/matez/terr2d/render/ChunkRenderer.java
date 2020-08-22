package net.matez.terr2d.render;

import net.matez.terr2d.block.Block;
import net.matez.terr2d.block.BlockRegistry;
import net.matez.terr2d.math.BlockPos;
import net.matez.terr2d.setup.Main;
import net.matez.terr2d.world.BlockColumn;
import net.matez.terr2d.world.Chunk;
import net.matez.terr2d.world.World;

import java.awt.image.BufferedImage;

public class ChunkRenderer {
    private boolean renderWater = false;
    private int blockSize = 1;
    private int renderVersion = 0;
    private int oldRenderVersion = 0;
    public ChunkRenderer(){

    }

    public void renderChunks(World world, Camera camera, int screenWidth, int screenHeight, boolean refresh){
        if(refresh){
            renderVersion = oldRenderVersion+1;
        }else{
            oldRenderVersion = renderVersion;
        }



        BlockPos cameraPos = camera.getLocation();
        int cameraX = cameraPos.getX();
        int cameraZ = cameraPos.getZ();

        int worldStartX = (int) Math.ceil((float) (cameraX - screenWidth / 2));
        int worldStartZ = (int) Math.ceil((float) (cameraZ - screenHeight / 2));

        int offsetX = calculateOffset(worldStartX);//0 -> 128
        int offsetZ = calculateOffset(worldStartZ);

        int renderStartX = 0;
        int renderStartZ = 0;
        int renderEndX = roundToChunkPos(screenWidth+offsetX);
        int renderEndZ = roundToChunkPos(screenHeight+offsetZ);

        for (int x = renderStartX; x < renderEndX; x += Chunk.CHUNK_SIZE) {
            for (int z = renderStartZ; z < renderEndZ; z += Chunk.CHUNK_SIZE) {
                int blockX = worldStartX + x;
                int blockZ = worldStartZ + z;

                Chunk chunk = world.getChunk(blockX, blockZ);
                if((!chunk.isRendered() || refresh || chunk.getRenderVersion()!=renderVersion) && chunk.isDirty()){
                    chunk.render(this);
                }
            }
        }
    }

    public int getRenderVersion() {
        return renderVersion;
    }

    private int calculateOffset(int i) {
        if(i<0){
            int a = Chunk.CHUNK_SIZE + (i % Chunk.CHUNK_SIZE);
            if(a==Chunk.CHUNK_SIZE){
                return 0;
            }
            return a;
        }
        return (i % Chunk.CHUNK_SIZE);
    }

    private int roundToChunkPos(int i){
        return (int) Math.ceil((double)i / Chunk.CHUNK_SIZE) * Chunk.CHUNK_SIZE;
    }

    private int roundToChunkPosDown(int i){
        return (int) Math.floor((double)i / Chunk.CHUNK_SIZE) * Chunk.CHUNK_SIZE;
    }


    public void setRenderWater(boolean renderWater) {
        this.renderWater = renderWater;
    }

    public boolean shouldRenderWater() {
        return renderWater;
    }

    public void setBlock(BlockColumn column,int width, int height, BufferedImage image, int x, int z){
        if (column != null && column.isDirty()) {
            if(column.getColumnMaxHeight()<63 && shouldRenderWater()) {
                renderBlock(width,height,image,x, column.getColumnMaxHeight(), z, blockSize, BlockRegistry.WATER);
            }else{
                renderBlock(width,height,image,x, column.getColumnMaxHeight(), z, blockSize, column.getBlock(column.getColumnMaxHeight()));
            }
        }
    }

    public void renderBlock(int width, int height, BufferedImage image, int x, int y, int z, int size, Block block){
        if(x>width){
            return;
        }
        if(z>height){
            return;
        }
        if(x<0){
            return;
        }
        if(z<0){
            return;
        }

        int rgba = block.getRgba();
        int b = (rgba)&0xFF;
        int g = (rgba>>8)&0xFF;
        int r = (rgba>>16)&0xFF;
        int a = (rgba>>24)&0xFF;


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
        int finalColor = createRGBA(r,g,b,a);
        for(int i = x; i < x + size; i++){
            for(int j = z; j < z + size; j++){
                if(i>=width){
                    break;
                }
                if(j>=height){
                    break;
                }
                image.setRGB(i, j, finalColor);
            }
        }
    }

    private int createRGBA(int r, int g, int b, int a)
    {
        return ((a & 0xff) << 24) + ((r & 0xff) << 16) + ((g & 0xff) << 8) + (b & 0xff);
    }

    public void renderBlock(int width, int height, BufferedImage image, int x, int z, int size, int rgb){
        if(x>width){
            return;
        }
        if(z>height){
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
                if(i>=width){
                    break;
                }
                if(j>=height){
                    break;
                }
                try {
                    image.setRGB(i, j, rgb);
                }catch (ArrayIndexOutOfBoundsException e){

                }
            }
        }
    }
}
