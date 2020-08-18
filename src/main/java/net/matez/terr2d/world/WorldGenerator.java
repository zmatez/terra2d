package net.matez.terr2d.world;

import net.matez.terr2d.block.BlockRegistry;
import net.matez.terr2d.math.BlockPos;
import net.matez.terr2d.math.ColumnPos;
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

    public void generate(Camera camera, int width, int height, float zoom){
        BlockPos cameraPos = camera.getLocation();
        int cameraX = cameraPos.getX();
        int cameraZ = cameraPos.getZ();
        int worldStartX = (int)Math.ceil((float)(cameraX - width/2) / zoom);
        int worldStartZ = (int)Math.ceil((float)(cameraZ - height/2) / zoom);
        int worldEndX = (int)Math.ceil((float)(cameraX + width/2) / zoom);
        int worldEndZ = (int)Math.ceil((float)(cameraZ + height/2) / zoom);

        for(int x = worldStartX; x < worldEndX; x++){
            for(int z = worldStartZ; z < worldEndZ; z++){
                generate(new BlockPos(x,0,z));
            }
        }
    }

    private void generate(BlockPos pos){
        if(!world.getColumn(new ColumnPos(pos.getX(),pos.getZ())).isDirty()) {
            float noiseVal = noise.GetSimplexFractal(pos.getX(), pos.getZ())*100;
            world.setBlock(pos.up((int) noiseVal), BlockRegistry.STONE);
            world.getColumn(new ColumnPos(pos.getX(), pos.getZ())).markDirty();
        }
    }
}
