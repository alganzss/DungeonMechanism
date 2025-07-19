package my.pikrew.MedievalRpg;

import org.bukkit.Location;
import org.bukkit.Particle;

public class Laser {

    private final Location start;
    private final Location end;

    public Laser(Location start, Location end) {
        this.start = start.clone().add(0.5, 0.5, 0.5);
        this.end = end.clone().add(0.5, 0.5, 0.5);
    }

    public Location getStart() {
        return start;
    }

    public Location getEnd() {
        return end;
    }

    public void display() {
        int steps = 50;
        for (int i = 0; i < steps; i++) {
            double t = (double) i / steps;
            double x = start.getX() + t * (end.getX() - start.getX());
            double y = start.getY() + t * (end.getY() - start.getY());
            double z = start.getZ() + t * (end.getZ() - start.getZ());
            start.getWorld().spawnParticle(Particle.SMOKE, new Location(start.getWorld(), x, y, z), 1);
        }
    }
}
