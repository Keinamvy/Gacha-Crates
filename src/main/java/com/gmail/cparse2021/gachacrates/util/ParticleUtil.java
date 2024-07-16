package com.gmail.cparse2021.gachacrates.util;

import com.gmail.cparse2021.gachacrates.GachaCrates;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ParticleUtil {
    public static void spawnCurvedLine(GachaCrates plugin, Location start, Location end, final Particle particle, final int count) {
        Random random = new Random();
        boolean negXOffset = random.nextBoolean();
        boolean negZOffset = random.nextBoolean();
        Location p0 = start.clone();
        Location p2 = end.clone();
        double x = p0.getX() + Math.random() * (double) (negXOffset ? -1 : 1);
        double y = p0.getY() + (p2.getY() - p0.getY());
        double z = p0.getZ() + Math.random() * (double) (negZOffset ? -1 : 1);
        Location p1 = new Location(start.getWorld(), x, y, z);
        final List<Location> curve = MathUtil.bezierCurve(100, p0, p1, p2);
        (new BukkitRunnable() {
            final Iterator<Location> locIterator = curve.iterator();

            public void run() {
                if (!this.locIterator.hasNext()) {
                    this.cancel();
                }

                Location particleLocation = this.locIterator.next();
                if (particleLocation.getWorld() != null) {
                    particleLocation.getWorld().spawnParticle(particle, particleLocation, count);
                }
            }
        }).runTaskTimer(plugin, 0L, 1L);
    }

    public static <T> void spawnCurvedLine(GachaCrates plugin, Location start, Location end, final Particle particle, final T data, final int count) {
        Random random = new Random();
        boolean negXOffset = random.nextBoolean();
        boolean negZOffset = random.nextBoolean();
        Location p0 = start.clone();
        Location p2 = end.clone();
        double x = p0.getX() + Math.random() * (double) (negXOffset ? -1 : 1);
        double y = p0.getY() + (p2.getY() - p0.getY());
        double z = p0.getZ() + Math.random() * (double) (negZOffset ? -1 : 1);
        Location p1 = new Location(start.getWorld(), x, y, z);
        final List<Location> curve = MathUtil.bezierCurve(21, p0, p1, p2);
        (new BukkitRunnable() {
            final Iterator<Location> locIterator = curve.iterator();
            final List<Location> particleLocations = new ArrayList<>();

            public void run() {
                if (!this.locIterator.hasNext()) {
                    this.cancel();
                } else {
                    this.particleLocations.add(this.locIterator.next());

                    for (Location particleLocation : this.particleLocations) {
                        if (particleLocation.getWorld() != null) {
                            particleLocation.getWorld().spawnParticle(particle, particleLocation, count, data);
                        }
                    }
                }
            }
        }).runTaskTimer(plugin, 0L, 1L);
    }

    public static <T> void spawnCircle(Location start, double radius, Particle particle, T data, int count, boolean hollow) {
        for (Location particleLocation : MathUtil.circle(start, radius, hollow)) {
            if (particleLocation.getWorld() != null) {
                particleLocation.getWorld().spawnParticle(particle, particleLocation, count, data);
            }
        }
    }

    public static <T> void spawnStraightLine(Location start, Location end, Particle particle, T data, int count) {
        Vector dir = end.clone().subtract(start).toVector();
        if (start.getWorld() != null) {
            for (double i = 0.1; i < start.distance(end); i += 0.1) {
                dir.multiply(i);
                start.add(dir);
                start.getWorld().spawnParticle(particle, start, count, data);
                start.subtract(dir);
                dir.normalize();
            }
        }
    }
}
