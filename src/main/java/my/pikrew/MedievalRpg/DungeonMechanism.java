package my.pikrew.MedievalRpg;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DungeonMechanism extends JavaPlugin implements Listener {

    private WorldGuardPlugin wg;
    private File configFile;
    private FileConfiguration config;

    private String regionName;
    private int radius;
    private Material triggerBlock;
    private long delay;

    @Override
    public void onEnable() {
        wg = getWorldGuard();
        Bukkit.getPluginManager().registerEvents(this, this);
        loadSettings();
        getLogger().info("Author:Pikrew");
        getLogger().info("DungeonMechanism enabled");
        getLogger().info("Door Room Dungeon Mechanism enabled");
        getLogger().info("Heal Arean Dungeon Mechanism Enabled");
        getLogger().info("Trap dungeon Mechanism Enabled");

    }

    private void loadSettings() {
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        regionName = config.getString("region", "dungeon");
        radius = config.getInt("radius", 1);
        triggerBlock = Material.matchMaterial(config.getString("trigger-block", "CHISELED_STONE_BRICKS"));
        delay = config.getLong("restore-delay", 6L);
    }

    private WorldGuardPlugin getWorldGuard() {
        return (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
    }

    private boolean isInRegion(Player player, String regionId) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(BukkitAdapter.adapt(player.getWorld()));
        if (manager == null) return false;

        ApplicableRegionSet set = manager.getApplicableRegions(BukkitAdapter.asBlockVector(player.getLocation()));
        for (ProtectedRegion region : set) {
            if (region.getId().equalsIgnoreCase(regionId)) return true;
        }
        return false;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null) return;
        if (e.getClickedBlock().getType() != triggerBlock) return;

        Player player = e.getPlayer();
        if (!isInRegion(player, regionName)) return;

        Block center = e.getClickedBlock();
        Map<Block, Material> originalBlocks = new HashMap<>();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Location loc = center.getLocation().clone().add(x, 0, z);
                Block block = loc.getBlock();
                originalBlocks.put(block, block.getType());
                block.setType(Material.AIR);
            }
        }

        Bukkit.getScheduler().runTaskLater(this, () -> {
            for (Map.Entry<Block, Material> entry : originalBlocks.entrySet()) {
                entry.getKey().setType(entry.getValue());
            }
        }, 20L * delay);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("dungeonreload")) {
            if (!sender.hasPermission("dungeon.reload")) {
                sender.sendMessage(ChatColor.RED + "Kamu tidak memiliki izin untuk menjalankan perintah ini.");
                return true;
            }

            sender.sendMessage(ChatColor.YELLOW + "⟳ Merefresh DungeonMechanism...");

            this.reloadConfig();
            this.loadSettings();
            this.loadRegionSettings();

            sender.sendMessage(ChatColor.GREEN + "✓ Plugin DungeonMechanism berhasil direload!");
            return true;
        }

        return false;
    }

    private void loadRegionSettings() {
    }

}
