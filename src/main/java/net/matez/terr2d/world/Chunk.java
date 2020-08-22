package net.matez.terr2d.world;

import net.matez.terr2d.block.Block;
import net.matez.terr2d.math.BlockPos;
import net.matez.terr2d.math.XZPos;
import net.matez.terr2d.render.ChunkRenderer;
import net.matez.terr2d.render.RenderUtils;
import net.matez.terr2d.setup.Main;

import java.awt.image.BufferedImage;

public class Chunk {
    public static int CHUNK_SIZE = 128;
    private final BlockColumn[][] columns = new BlockColumn[CHUNK_SIZE][CHUNK_SIZE];
    private final XZPos chunkPos;
    private BufferedImage chunkRender;
    private boolean isRendered = false;
    private int renderVersion = 0;
    private boolean isDirty = false;

    public Chunk(XZPos chunkPos) {
        this.chunkPos = chunkPos;
    }

    public XZPos getChunkPos() {
        return chunkPos;
    }

    public void render(ChunkRenderer renderer) {
        chunkRender = RenderUtils.createHardwareAcceleratedImage(CHUNK_SIZE, CHUNK_SIZE, true);
        renderVersion = renderer.getRenderVersion();
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                renderer.setBlock(getColumnRelative(x,z),CHUNK_SIZE,CHUNK_SIZE,chunkRender,x,z);
            }
        }
        isRendered = true;
    }

    public BufferedImage getChunkRender() {
        return chunkRender;
    }

    public boolean isRendered() {
        return isRendered;
    }

    public int getRenderVersion() {
        return renderVersion;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void markDirty(){
        isDirty = true;
    }

    public BlockColumn getColumn(XZPos pos) {
        return getColumn(pos.getX(), pos.getZ());
    }

    public BlockColumn getColumn(BlockPos pos) {
        return getColumn(pos.getX(), pos.getZ());
    }

    public BlockColumn getColumnRelative(XZPos pos) {
        return getColumnRelative(pos.getX(), pos.getZ());
    }

    public BlockColumn getColumnRelative(BlockPos pos) {
        return getColumnRelative(pos.getX(), pos.getZ());
    }

    public BlockColumn getColumn(int x, int z) {
        int relX = getRelativePos(x, chunkPos.getX());
        int relZ = getRelativePos(z, chunkPos.getZ());

        if (relX < 0) {
            Main.LOGGER.error("Cannot get block column at pos " + x + "x" + z + " rel: " + relX + "x" + relZ + ": x < 0" + ": chunkPos " + chunkPos.getX() + "x" + chunkPos.getZ());
            return null;
        } else if (relZ < 0) {
            Main.LOGGER.error("Cannot get block column at pos " + x + "x" + z + " rel: " + relX + "x" + relZ + ": z < 0" + ": chunkPos " + chunkPos.getX() + "x" + chunkPos.getZ());
            return null;
        } else if (relX > CHUNK_SIZE) {
            Main.LOGGER.error("Cannot get block column at pos " + x + "x" + z + " rel: " + relX + "x" + relZ + ": x > " + CHUNK_SIZE + ": chunkPos " + chunkPos.getX() + "x" + chunkPos.getZ());
            return null;
        } else if (relZ > CHUNK_SIZE) {
            Main.LOGGER.error("Cannot get block column at pos " + x + "x" + z + " rel: " + relX + "x" + relZ + ": z > " + CHUNK_SIZE + ": chunkPos " + chunkPos.getX() + "x" + chunkPos.getZ());
            return null;
        }
        return getColumnRelative(relX,relZ);
    }

    public BlockColumn getColumnRelative(int x, int z) {
        if (x < 0) {
            Main.LOGGER.error("Cannot get block column at pos " + x + "x" + z + ": x < 0" + ": chunkPos " + chunkPos.getX() + "x" + chunkPos.getZ());
            return null;
        } else if (z < 0) {
            Main.LOGGER.error("Cannot get block column at pos " + x + "x" + z + ": z < 0" + ": chunkPos " + chunkPos.getX() + "x" + chunkPos.getZ());
            return null;
        } else if (x > CHUNK_SIZE) {
            Main.LOGGER.error("Cannot get block column at pos " + x + "x" + z + ": x > " + CHUNK_SIZE + ": chunkPos " + chunkPos.getX() + "x" + chunkPos.getZ());
            return null;
        } else if (z > CHUNK_SIZE) {
            Main.LOGGER.error("Cannot get block column at pos " + x + "x" + z + ": z > " + CHUNK_SIZE + ": chunkPos " + chunkPos.getX() + "x" + chunkPos.getZ());
            return null;
        }

        try {
            BlockColumn column = columns[x][z];
            if (column == null) {
                column = new BlockColumn(new XZPos(x, z));
                columns[x][z] = column;
            }
            return column;
        } catch (Exception e) {
            Main.LOGGER.fatal("ARR: " + x + "x" + z + ": " + columns.length + "x" + columns[x].length);
        }
        return null;
    }

    private int getRelativePos(int i, int chunkPos) {
        if (i >= 0) {
            return i - chunkPos;
        } else {
            return Math.abs(chunkPos - i);
        }
    }

    public Block getBlock(BlockPos pos){
        return getColumn(pos).getBlock(pos.getY());
    }

    public void setBlock(BlockPos pos, Block state){
        getColumn(pos).setBlock(pos.getY(),state);
    }

    public Block getBlockRelative(BlockPos pos){
        return getColumnRelative(pos).getBlock(pos.getY());
    }

    public void setBlockRelative(BlockPos pos, Block state){
        getColumnRelative(pos).setBlock(pos.getY(),state);
    }

}
