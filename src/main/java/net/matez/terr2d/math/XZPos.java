package net.matez.terr2d.math;

import java.util.Objects;

public class XZPos {
    private int x,z;
    public XZPos(int x, int z){
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

    public static XZPos add(XZPos vec1, XZPos vec2){
        return new XZPos(vec1.getX() + vec2.getX(), vec1.getZ() + vec2.getZ());
    }

    public static XZPos subtract(XZPos vec1, XZPos vec2){
        return new XZPos(vec1.getX() - vec2.getX(), vec1.getZ() - vec2.getZ());
    }

    public static XZPos multiply(XZPos vec1, XZPos vec2){
        return new XZPos(vec1.getX() * vec2.getX(), vec1.getZ() * vec2.getZ());
    }

    public static XZPos divide(XZPos vec1, XZPos vec2){
        return new XZPos(vec1.getX() / vec2.getX(), vec1.getZ() / vec2.getZ());
    }

    public static int length(XZPos vector){
        return (int)Math.sqrt(vector.getX() * vector.getX() + vector.getZ() * vector.getZ());
    }

    public static XZPos normalize(XZPos vector){
        int len = XZPos.length(vector);
        return XZPos.divide(vector,new XZPos(len,len));
    }

    public static int dot(XZPos vec1, XZPos vec2) {
        return vec1.getX() * vec2.getX() + vec1.getZ() * vec2.getZ();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XZPos columnPos = (XZPos) o;
        return x == columnPos.x &&
                z == columnPos.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }
}
