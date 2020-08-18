package net.matez.terr2d.math;

import java.util.Objects;

public class ColumnPos {
    private int x,z;
    public ColumnPos(int x, int z){
        this.x=x;
        this.z=z;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public void set(int x, int z){
        this.x=x;
        this.z=z;
    }

    public void add(int x, int z){
        this.x+=x;
        this.z+=z;
    }

    public static ColumnPos add(ColumnPos vec1, ColumnPos vec2){
        return new ColumnPos(vec1.getX() + vec2.getX(), vec1.getZ() + vec2.getZ());
    }

    public static ColumnPos subtract(ColumnPos vec1, ColumnPos vec2){
        return new ColumnPos(vec1.getX() - vec2.getX(), vec1.getZ() - vec2.getZ());
    }

    public static ColumnPos multiply(ColumnPos vec1, ColumnPos vec2){
        return new ColumnPos(vec1.getX() * vec2.getX(), vec1.getZ() * vec2.getZ());
    }

    public static ColumnPos divide(ColumnPos vec1, ColumnPos vec2){
        return new ColumnPos(vec1.getX() / vec2.getX(), vec1.getZ() / vec2.getZ());
    }

    public static int length(ColumnPos vector){
        return (int)Math.sqrt(vector.getX() * vector.getX() + vector.getZ() * vector.getZ());
    }

    public static ColumnPos normalize(ColumnPos vector){
        int len = ColumnPos.length(vector);
        return ColumnPos.divide(vector,new ColumnPos(len,len));
    }

    public static int dot(ColumnPos vec1, ColumnPos vec2) {
        return vec1.getX() * vec2.getX() + vec1.getZ() * vec2.getZ();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnPos columnPos = (ColumnPos) o;
        return x == columnPos.x &&
                z == columnPos.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }
}
