package net.matez.terr2d.render;

import net.matez.terr2d.math.BlockPos;
import net.matez.terr2d.math.MathHelper;
import net.matez.terr2d.math.XZPos;
import net.matez.terr2d.setup.Main;
import net.matez.terr2d.world.Chunk;
import net.matez.terr2d.world.World;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class WorldRenderer {
    private int width, height;
    private BufferedImage bufferedImage;
    private Graphics2D graphics;

    public void create(int width, int height) {
        Main.LOGGER.debug("Creating buffered image with size: " + width + "x" + height);
        bufferedImage = RenderUtils.createHardwareAcceleratedImage(width, height, true);
        graphics = (Graphics2D)bufferedImage.getGraphics();
        this.width = width;
        this.height = height;
    }

    public void render(World world, Camera camera,  boolean renderChunkData, boolean refresh, float zoom) {
        if(refresh) {
            graphics.clearRect(0, 0, width, height);
        }

        graphics.setStroke(new BasicStroke((float)Math.ceil(zoom)));
        int zoomedFontSize = (int)Math.ceil(zoom * 14);
        int fontSize = Math.max(Math.min(zoomedFontSize, Chunk.CHUNK_SIZE),10);
        graphics.setFont(new Font("Consolas",Font.PLAIN,fontSize));

        BlockPos cameraPos = camera.getLocation();
        int cameraX = cameraPos.getX();
        int cameraZ = cameraPos.getZ();

        int worldStartX = (int) Math.ceil((float) (cameraX - width / 2));
        int worldStartZ = (int) Math.ceil((float) (cameraZ - height / 2));

        int offsetX = calculateOffset(worldStartX);//0 -> 128
        int offsetZ = calculateOffset(worldStartZ);

        int renderStartX = 0;
        int renderStartZ = 0;
        int renderEndX = roundToChunkPos(width+offsetX);
        int renderEndZ = roundToChunkPos(height+offsetZ);

        for (int x = renderStartX; x < renderEndX; x += Chunk.CHUNK_SIZE) {
            for (int z = renderStartZ; z < renderEndZ; z += Chunk.CHUNK_SIZE) {
                int blockX = worldStartX + x;
                int blockZ = worldStartZ + z;



                Chunk chunk = world.getChunk(blockX, blockZ);
                int relX = x - offsetX;
                int relZ = z - offsetZ;

                if (chunk.isRendered()) {
                    graphics.drawImage(chunk.getChunkRender(),relX,relZ,null);
                    if(renderChunkData) {
                        graphics.setColor(Color.GREEN);
                        graphics.drawRect(relX, relZ, Chunk.CHUNK_SIZE, Chunk.CHUNK_SIZE);
                        graphics.drawString(chunk.getChunkPos().getX() + "x" + chunk.getChunkPos().getZ(), relX + 10, relZ + 10 + (fontSize/2));
                    }
                }else{
                    graphics.setColor(Color.BLACK);
                    graphics.fillRect(relX,relZ,Chunk.CHUNK_SIZE,Chunk.CHUNK_SIZE);
                    graphics.setColor(Color.RED);
                    graphics.drawRect(relX,relZ,Chunk.CHUNK_SIZE,Chunk.CHUNK_SIZE);
                }
            }
        }
    }

    public XZPos convertScreenToWorldPos(int x, int z, Camera camera){
        BlockPos cameraPos = camera.getLocation();
        int cameraX = cameraPos.getX();
        int cameraZ = cameraPos.getZ();

        int worldStartX = (int) Math.ceil((float) (cameraX - width / 2));
        int worldStartZ = (int) Math.ceil((float) (cameraZ - height / 2));
        return new XZPos(x + worldStartX, z + worldStartZ);
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private int roundToChunkPos(int i) {
        return (int) Math.ceil((double) i / Chunk.CHUNK_SIZE) * Chunk.CHUNK_SIZE;
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

}
