package org.emil.hnrpmc.hnessentials.cosmetics.api;

import java.util.Objects;

/**
 * A bounding box constructed from 2 corners. Objects of this type are required to have {@code x0 <= x1}, {@code y0 <= y1}, {@code z0 <= z1}.
 */
public final class Box {
    public Box(double x0, double y0, double z0,
               double x1, double y1, double z1) {
        this.x0 = x0;
        this.y0 = y0;
        this.z0 = z0;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
    }

    private final double x0;
    private final double y0;
    private final double z0;
    private final double x1;
    private final double y1;
    private final double z1;

    /**
     * Checks whether the given coordinate is inside this bounding box.
     * @param x the x position of the coordinate.
     * @param y the y position of the coordinate.
     * @param z the z position of the coordinate.
     * @return whether the given coordinate is inside this bounding box.
     */
    public boolean isInBox(double x, double y, double z) {
        return this.x0 <= x && x <= this.x1
                && this.y0 <= y && y <= this.y1
                && this.z0 <= z && z <= this.z1;
    }

    /**
     * @return the size in the x direction of this box.
     */
    public double getWidth() {
        return this.x1 - this.x0;
    }

    /**
     * @return the size in the y direction of this box.
     */
    public double getHeight() {
        return this.y1 - this.y0;
    }

    /**
     * @return the size in the z direction of this box.
     */
    public double getDepth() {
        return this.z1 - this.z0;
    }

    public double x0() {
        return x0;
    }

    public double y0() {
        return y0;
    }

    public double z0() {
        return z0;
    }

    public double x1() {
        return x1;
    }

    public double y1() {
        return y1;
    }

    public double z1() {
        return z1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        Box that = (Box) obj;
        return this.x0 == that.x0
                && this.y0 == that.y0
                && this.z0 == that.z0
                && this.x1 == that.x1
                && this.y1 == that.y1
                && this.z1 == that.z1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x0, y0, z0, x1, y1, z1);
    }

    @Override
    public String toString() {
        return "Box[" +
                "x0=" + x0 + ", " +
                "y0=" + y0 + ", " +
                "z0=" + z0 + ", " +
                "x1=" + x1 + ", " +
                "y1=" + y1 + ", " +
                "z1=" + z1 + ']';
    }

}
