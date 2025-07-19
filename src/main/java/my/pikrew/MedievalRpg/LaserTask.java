package my.pikrew.MedievalRpg;

import org.bukkit.scheduler.BukkitRunnable;

public class LaserTask extends BukkitRunnable {

    private final TrapLaser plugin;
    private boolean active = true;

    public LaserTask(TrapLaser plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (active) {
            plugin.getLaserManager().showLasers(true);
        }
        active = !active;
    }
}

