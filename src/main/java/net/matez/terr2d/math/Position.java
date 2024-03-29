package net.matez.terr2d.math;

public class Position implements IPosition {
   protected final double x;
   protected final double y;
   protected final double z;

   public Position(double xCoord, double yCoord, double zCoord) {
      this.x = xCoord;
      this.y = yCoord;
      this.z = zCoord;
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getZ() {
      return this.z;
   }
}