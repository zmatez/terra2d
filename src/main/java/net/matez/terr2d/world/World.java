package net.matez.terr2d.world;

import net.matez.terr2d.block.Block;
import net.matez.terr2d.math.BlockPos;
import net.matez.terr2d.math.ColumnPos;

import java.util.HashMap;
import java.util.Map;

public class World {
    private Map<ColumnPos, BlockColumn> columns;
    private int seaLevel = 63;
    private int genMinX, genMinZ, genMaxX, genMaxZ;
    private long seed = 2137L;
    public World(){
        columns = new HashMap<>();
    }

    public BlockColumn getColumn(ColumnPos pos){
        try {
            BlockColumn column = columns.get(pos);
            if (column == null) {
                column = new BlockColumn(pos);
                columns.put(pos, column);
                if(genMinX > pos.getX()){
                    genMinX = pos.getX();
                }
                if(genMinZ > pos.getZ()){
                    genMinZ = pos.getZ();
                }
                if(genMaxX < pos.getX()){
                    genMaxX = pos.getX();
                }
                if(genMaxZ < pos.getZ()){
                    genMaxZ = pos.getZ();
                }
            }
            return column;
        }catch (ClassCastException e){
            return null;
        }
    }

    public Block getBlock(BlockPos pos){
        BlockColumn column = getColumn(new ColumnPos(pos.getX(),pos.getZ()));
        return column.getBlock(pos.getY());
    }

    public void setBlock(BlockPos pos, Block state){
        BlockColumn column = getColumn(new ColumnPos(pos.getX(),pos.getZ()));
        column.setBlock(pos.getY(),state);
    }

    public int getSeaLevel() {
        return seaLevel;
    }

    public int[] getGeneratedWorldSize(){
        int[] i = new int[2];
        i[0] = Math.abs(genMinX)+genMaxX+1;
        i[1] = Math.abs(genMinZ)+genMaxZ+1;
        return i;
    }
}
