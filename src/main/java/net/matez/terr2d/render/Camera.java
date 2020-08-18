package net.matez.terr2d.render;

import net.matez.terr2d.math.BlockPos;

public class Camera {
    public BlockPos location = BlockPos.ZERO;
    public Camera(){

    }

    public BlockPos getLocation() {
        return location;
    }

    public void setLocation(BlockPos location) {
        this.location = location;
    }
}
