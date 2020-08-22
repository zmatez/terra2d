package net.matez.terr2d.render;

import net.matez.terr2d.math.BlockPos;
import net.matez.terr2d.math.XZPos;
import net.matez.terr2d.world.World;

public class WorldScreenBuffer {
    private int oldWidth, oldHeight;
    private int oldDetails;
    private float oldZoom;
    private BlockPos oldCameraPos;

    public void refresh(World world, Camera camera, boolean enableY, int details, float zoom, int width, int height){
        oldWidth = width;
        oldHeight = height;
        oldDetails = details;
        oldZoom = zoom;
        oldCameraPos = camera.getLocation();
    }

    public boolean needsUpdate(World world, int x, int z, Camera camera, boolean enableY, int details, float zoom, int width, int height){
        boolean needs = false;
        if(oldWidth!=width){
            needs = true;
        }else if(oldHeight!=height){
            needs = true;
        }else if(camera.getLocation()!=oldCameraPos){
            needs = true;
        }else if(oldDetails!=details){
            needs = true;
        }else if(oldZoom!=zoom){
            needs = true;
        }else if(!world.getChunk(x,z).getColumn(x,z).isDirty()){
            needs = true;
        }
        return needs;
    }

}
