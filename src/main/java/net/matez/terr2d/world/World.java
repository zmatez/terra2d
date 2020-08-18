package net.matez.terr2d.world;

import net.matez.terr2d.block.Block;
import net.matez.terr2d.math.BlockPos;
import net.matez.terr2d.math.ColumnPos;

import java.util.HashMap;
import java.util.Map;

public class World {
    private Map<ColumnPos, BlockColumn> columns;
    public World(){
        columns = new HashMap<>();
    }

    public BlockColumn getColumn(ColumnPos pos){
        BlockColumn column = null;
        if(columns.containsKey(pos)) {
            column = columns.get(pos);
        }else{
            column=new BlockColumn(pos);
            columns.put(pos,column);
        }
        return column;
    }

    public Block getBlock(BlockPos pos){
        BlockColumn column = getColumn(new ColumnPos(pos.getX(),pos.getZ()));
        return column.getBlock(pos.getY());
    }

    public void setBlock(BlockPos pos, Block state){
        BlockColumn column = getColumn(new ColumnPos(pos.getX(),pos.getZ()));
        column.setBlock(pos.getY(),state);
    }
}
