package my.pikrew.MedievalRpg;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LaserManager {

    private final TrapLaser plugin;
    private final List<Laser> lasers = new ArrayList<>();
    private final File file;
    private final YamlConfiguration config;

    public LaserManager(TrapLaser plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "lasers.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void createLaser(Block first, Block second) {
        lasers.add(new Laser(first.getLocation(), second.getLocation()));
    }

    public void showLasers(boolean show) {
        if (!show) return;
        for (Laser laser : lasers) {
            laser.display();
        }
    }

    public void loadLasers() {
        lasers.clear();
        if (config.contains("lasers")) {
            for (String key : config.getConfigurationSection("lasers").getKeys(false)) {
                Location a = (Location) config.get("lasers." + key + ".a");
                Location b = (Location) config.get("lasers." + key + ".b");
                lasers.add(new Laser(a, b));
            }
        }
    }

    public void saveLasers() {
        config.set("lasers", null);
        int i = 0;
        for (Laser laser : lasers) {
            config.set("lasers." + i + ".a", laser.getStart());
            config.set("lasers." + i + ".b", laser.getEnd());
            i++;
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
