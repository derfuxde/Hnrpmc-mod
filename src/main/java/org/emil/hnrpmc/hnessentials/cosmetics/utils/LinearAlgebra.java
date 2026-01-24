package org.emil.hnrpmc.hnessentials.cosmetics.utils;

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class LinearAlgebra {
    public static final Vector3f XP = new Vector3f(1, 0, 0);
    public static final Vector3f XN = new Vector3f(-1, 0, 0);

    public static final Vector3f YP = new Vector3f(0, 1, 0);
    public static final Vector3f YN = new Vector3f(0, -1, 0);

    public static final Vector3f ZP = new Vector3f(0, 0, 1);
    public static final Vector3f ZN = new Vector3f(0, 0, -1);

    public static final Quaternionf QUATERNION_ONE = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);

    public static Quaternionf quaternion(Vector3f axis, float radians) {
        return new Quaternionf(new AxisAngle4f(radians, axis));
    }

    public static Quaternionf quaternionDegrees(Vector3f axis, float degrees) {
        return new Quaternionf(new AxisAngle4f((float)Math.toRadians(degrees), axis));
    }
}