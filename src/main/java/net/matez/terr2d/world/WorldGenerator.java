package net.matez.terr2d.world;

import net.matez.terr2d.block.BlockRegistry;
import net.matez.terr2d.math.BlockPos;
import net.matez.terr2d.noise.FastNoise;
import net.matez.terr2d.render.Camera;

public class WorldGenerator {
    private World world;
    private FastNoise noise = new FastNoise();
    public WorldGenerator(World world){
        this.world=world;
        noise.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
        noise.SetFrequency(0.005f);
        noise.SetFractalOctaves(5);
        noise.SetFractalLacunarity(3);
        noise.SetFractalGain(0.5f);
        noise.SetFractalType(FastNoise.FractalType.FBM);
    }

    public void generate(Camera camera, int width, int height){
        BlockPos cameraPos = camera.getLocation();
        int cameraX = cameraPos.getX();
        int cameraZ = cameraPos.getZ();
        int worldStartX = (int)Math.ceil((float)(cameraX - width/2));
        int worldStartZ = (int)Math.ceil((float)(cameraZ - height/2));
        int offsetX = calculateOffset(worldStartX);//0 -> 128
        int offsetZ = calculateOffset(worldStartZ);
        int worldEndX = roundToChunkPos((int)Math.ceil((float)(cameraX + width/2))+offsetX);
        int worldEndZ = roundToChunkPos((int)Math.ceil((float)(cameraZ + height/2))+offsetZ);

        for(int x = worldStartX; x < worldEndX; x+=Chunk.CHUNK_SIZE){
            for(int z = worldStartZ; z < worldEndZ; z+=Chunk.CHUNK_SIZE){
                Chunk chunk = world.getChunk(x,z);
                if(!chunk.isDirty()) {
                    generate(chunk);
                }
            }
        }
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


    private void generate(Chunk chunk){
        for(int x = 0; x < Chunk.CHUNK_SIZE; x++){
            for(int z = 0; z < Chunk.CHUNK_SIZE; z++){
                BlockColumn column = chunk.getColumnRelative(x,z);
                if(!column.isDirty()){
                    float noiseVal = noise.GetSimplexFractal(chunk.getChunkPos().getX()+x, chunk.getChunkPos().getZ()+z)*100;
                    BlockPos pos = new BlockPos(x,0,z);
                    pos = pos.up((int) noiseVal);
                    chunk.setBlockRelative(pos, BlockRegistry.STONE);
                    column.markDirty();
                }
            }
        }
        chunk.markDirty();
    }
}
