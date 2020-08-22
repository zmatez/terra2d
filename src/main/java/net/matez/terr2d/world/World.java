package net.matez.terr2d.world;

import net.matez.terr2d.block.Block;
import net.matez.terr2d.math.BlockPos;
import net.matez.terr2d.math.XZPos;
import net.matez.terr2d.setup.Main;

import java.util.HashMap;
import java.util.Map;

public class World {
    private HashMap<XZPos, Chunk> chunks;
    private int seaLevel = 63;
    private int genMinX, genMinZ, genMaxX, genMaxZ;
    private long seed = 2137L;
    public World(){
        chunks = new HashMap<XZPos, Chunk>();
    }

    public Chunk getChunk(XZPos pos){
        return getChunk(pos.getX(),pos.getZ());
    }

    public Chunk getChunk(BlockPos pos){
        return getChunk(pos.getX(),pos.getZ());
    }

    public Chunk getChunk(int x, int z){
        try {
            XZPos chunkPos = new XZPos(roundToChunkPos(x),roundToChunkPos(z));
            Chunk chunk = chunks.get(chunkPos);
            if (chunk == null) {
                chunk = new Chunk(chunkPos);
                chunks.put(chunkPos, chunk);
            }
            return chunk;
        }catch (ClassCastException e){
            Main.LOGGER.debug("Error");
            return null;
        }
    }

    private int roundToChunkPos(int i){
        if(i>=0) {
            return (int) Math.floor((double)i / Chunk.CHUNK_SIZE) * Chunk.CHUNK_SIZE;
        }else{
            return -((int) Math.ceil((double)Math.abs(i) / Chunk.CHUNK_SIZE) * Chunk.CHUNK_SIZE);
        }
    }

    public Block getBlock(BlockPos pos){
        return getChunk(pos).getColumn(pos).getBlock(pos.getY());
    }

    public void setBlock(BlockPos pos, Block state){
        getChunk(pos).getColumn(pos).setBlock(pos.getY(),state);
    }

    public int getSeaLevel() {
        return seaLevel;
    }

    /*public int[] getGeneratedWorldSize(){
        int[] i = new int[2];
        i[0] = Math.abs(genMinX)+genMaxX+1;
        i[1] = Math.abs(genMinZ)+genMaxZ+1;
        return i;
    }*/
}
