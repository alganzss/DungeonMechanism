package my.pikrew.MedievalRpg;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class HealArea extends BukkitRunnable {

    private final DungeonMechanism plugin;

    public HealArea(DungeonMechanism plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Material healBlock = Material.getMaterial(plugin.getConfig().getString("heal-block", "LAPIS_BLOCK"));
        double healAmount = plugin.getConfig().getDouble("heal-amount", 1.0);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isInRegion(player, "dungeon")) continue;

            Block below = player.getLocation().subtract(0, 1, 0).getBlock();
            if (below.getType() != healBlock) continue;

            if (player.getHealth() < player.getMaxHealth()) {
                double newHealth = Math.min(player.getHealth() + healAmount, player.getMaxHealth());
                player.setHealth(newHealth);
            }
        }
    }

    private boolean isInRegion(Player player, String regionId) {
        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld()));
        if (manager == null) return false;

        ApplicableRegionSet set = manager.getApplicableRegions(BukkitAdapter.asBlockVector(player.getLocation()));
        for (ProtectedRegion region : set) {
            if (region.getId().equalsIgnoreCase(regionId)) return true;
        }
        return false;
    }
}

