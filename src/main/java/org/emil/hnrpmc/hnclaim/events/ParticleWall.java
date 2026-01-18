package org.emil.hnrpmc.hnclaim.events;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class ParticleWall {
    private final ServerLevel level;
    private final Vec3 p1, p2, p3, p4;
    private int remainingTicks;

    public ParticleWall(ServerLevel level, Vec3 p1, Vec3 p2, Vec3 p3, Vec3 p4, int duration) {
        this.level = level;
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        this.p4 = p4;
        this.remainingTicks = duration;
    }

    // Diese Methode wird jeden Server-Tick aufgerufen
    public boolean tick() {
        if (remainingTicks <= 0) return false;

        // Rendere die Wand z.B. alle 2 Ticks für bessere Performance
        if (remainingTicks % 2 == 0) {
            render();
        }

        remainingTicks--;
        return true;
    }

    private void render() {
        drawEdge(p1, p2);
        drawEdge(p2, p3);
        drawEdge(p3, p4);
        drawEdge(p4, p1);
    }

    private void drawEdge(Vec3 start, Vec3 end) {
        double dist = start.distanceTo(end);
        double step = 0.5; // Alle 0.5 Blöcke ein Partikel

        for (double d = 0; d < dist; d += step) {
            double t = d / dist;
            double x = start.x + (end.x - start.x) * t;
            double y = start.y + (end.y - start.y) * t;
            double z = start.z + (end.z - start.z) * t;

            level.sendParticles(ParticleTypes.GLOW, x, y, z, 1, 0, 0, 0, 0);
        }
    }
}