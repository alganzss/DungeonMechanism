package my.pikrew.MedievalRpg;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class TrapLaser extends JavaPlugin {

    private static TrapLaser instance;
    private LaserManager laserManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.laserManager = new LaserManager(this);

        Bukkit.getPluginManager().registerEvents(new WandListener(this), this);
        new LaserTask(this).runTaskTimer(this, 0L, 40L); // 2 detik (40 tick)

        getCommand("trapwand").setExecutor((sender, command, label, args) -> {
            WandListener.giveTrapWand(sender);
            return true;
        });

        laserManager.loadLasers();
    }

    @Override
    public void onDisable() {
        laserManager.saveLasers();
    }

    public static TrapLaser getInstance() {
        return instance;
    }

    public LaserManager getLaserManager() {
        return laserManager;
    }
}
