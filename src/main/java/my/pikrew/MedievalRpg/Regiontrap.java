package my.pikrew.MedievalRpg;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Regiontrap implements Listener {

    private final JavaPlugin plugin;

    public Regiontrap(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();

        if (loc.getBlock().getType() == Material.STONECUTTER) {
            if (isInRegion(player, "dg1")) {
                trapPlayer(player, loc);
            }
        }
    }

    private void trapPlayer(Player player, Location loc) {
        // Tampilkan kotak partikel selama 5 detik
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (counter >= 5) {
                    this.cancel();
                    return;
                }
                drawParticleBox(loc, Particle.SNOWFLAKE);
                counter++;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        // Membekukan gerakan
        player.setWalkSpeed(0f);
        player.setFlySpeed(0f);

        new BukkitRunnable() {
            @Override
            public void run() {
                player.setWalkSpeed(0.2f);
                player.setFlySpeed(0.1f);
            }
        }.runTaskLater(plugin, 100L);
    }

    private void drawParticleBox(Location loc, Particle particle) {
        double minX = loc.getX() - 0.5;
        double maxX = loc.getX() + 1.5;
        double minY = loc.getY();
        double maxY = loc.getY() + 2;
        double minZ = loc.getZ() - 0.5;
        double maxZ = loc.getZ() + 1.5;

        for (double x = minX; x <= maxX; x += 0.5) {
            for (double y = minY; y <= maxY; y += 0.5) {
                for (double z = minZ; z <= maxZ; z += 0.5) {
                    if (x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ) {
                        Location particleLoc = new Location(loc.getWorld(), x, y, z);
                        loc.getWorld().spawnParticle(particle, particleLoc, 1, 0, 0, 0, 0);
                    }
                }
            }
        }
    }

    private boolean isInRegion(Player player, String regionId) {
        WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (wg == null) return false;

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(BukkitAdapter.adapt(player.getWorld()));
        if (manager == null) return false;

        com.sk89q.worldedit.util.Location wgLoc = BukkitAdapter.adapt(player.getLocation());
        ApplicableRegionSet regions = manager.getApplicableRegions(wgLoc.toVector().toBlockPoint());

        for (ProtectedRegion region : regions) {
            if (region.getId().equalsIgnoreCase(regionId)) {
                return true;
            }
        }

        return false;
    }
}
