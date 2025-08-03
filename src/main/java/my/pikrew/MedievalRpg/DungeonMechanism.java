package my.pikrew.MedievalRpg;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import my.pikrew.MedievalRpg.ConfigGui.ConfigGUI;
import my.pikrew.MedievalRpg.ConfigGui.ConfigGUICommand;
import my.pikrew.mmoitemsdungeon.RegionEntryListener;
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
    private ConfigGUI configGUI;

    private String regionName;
    private int radius;
    private Material triggerBlock;
    private long delay;

    @Override
    public void onEnable() {
        wg = getWorldGuard();

        // Initialize GUI system
        configGUI = new ConfigGUI(this);

        // Register events
        Bukkit.getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(configGUI, this);

        loadSettings();

        getLogger().info("Author:Pikrew");
        getLogger().info("DungeonMechanism enabled");
        getLogger().info("Door Room Dungeon Mechanism enabled");
        getLogger().info("Heal Arena Dungeon Mechanism Enabled");
        getLogger().info("GUI Configuration System enabled");

        getServer().getPluginManager().registerEvents(new RegionEntryListener(getLogger()), this);
        getServer().getPluginManager().registerEvents(new Regiontrap(this), this);

        saveDefaultConfig();

        // Register existing listeners
        getServer().getPluginManager().registerEvents(new Regiontrap(this), this);
        // Register death respawn listener
        getServer().getPluginManager().registerEvents(new DeathEvent(this), this);

        // Register command executors
        getCommand("dungeonspawn").setExecutor(new DeathEventCommand(this));

        // Register GUI command
        ConfigGUICommand guiCommand = new ConfigGUICommand(this, configGUI);
        getCommand("dconfig").setExecutor(guiCommand);
        getCommand("dconfig").setTabCompleter(guiCommand);

        getLogger().info("DungeonMechanism plugin telah diaktifkan!");
        getLogger().info("Death Respawn System untuk dungeon telah dimuat!");
        getLogger().info("GUI Configuration System telah dimuat!");
        getLogger().info("Commands registered: /dconfig, /dungeonconfig, /dcfg");
        getLogger().info("Dungeon spawn point: " +
                getConfig().getDouble("dungeon_spawn.x", 183) + ", " +
                getConfig().getDouble("dungeon_spawn.y", 4) + ", " +
                getConfig().getDouble("dungeon_spawn.z", 16));
    }

    @Override
    public void onDisable() {
        getLogger().info("DungeonMechanism plugin telah dinonaktifkan!");
        getLogger().info("GUI Configuration System telah dinonaktifkan!");
    }

    public void loadSettings() {
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        regionName = config.getString("region", "dungeon");
        radius = config.getInt("radius", 1);

        String triggerBlockName = config.getString("trigger-block", "CHISELED_STONE_BRICKS");
        triggerBlock = Material.matchMaterial(triggerBlockName);
        if (triggerBlock == null) {
            getLogger().warning("Invalid trigger block: " + triggerBlockName + ", using CHISELED_STONE_BRICKS");
            triggerBlock = Material.CHISELED_STONE_BRICKS;
        }

        delay = config.getLong("restore-delay", 6L);

        getLogger().info("Settings loaded:");
        getLogger().info("- Region: " + regionName);
        getLogger().info("- Radius: " + radius);
        getLogger().info("- Trigger Block: " + triggerBlock.name());
        getLogger().info("- Restore Delay: " + delay + "s");
    }

    public void loadRegionSettings() {
        // Load region-specific settings if needed
        // This method can be expanded for additional region configurations
        getLogger().info("Region settings loaded successfully!");
    }

    private WorldGuardPlugin getWorldGuard() {
        return (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
    }

    private boolean isInRegion(Player player, String regionId) {
        if (wg == null) {
            getLogger().warning("WorldGuard not found!");
            return false;
        }

        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager manager = container.get(BukkitAdapter.adapt(player.getWorld()));
            if (manager == null) return false;

            ApplicableRegionSet set = manager.getApplicableRegions(BukkitAdapter.asBlockVector(player.getLocation()));
            for (ProtectedRegion region : set) {
                if (region.getId().equalsIgnoreCase(regionId)) return true;
            }
        } catch (Exception e) {
            getLogger().warning("Error checking region: " + e.getMessage());
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

        // Create temporary hole
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Location loc = center.getLocation().clone().add(x, 0, z);
                Block block = loc.getBlock();
                originalBlocks.put(block, block.getType());
                block.setType(Material.AIR);
            }
        }

        // Schedule restoration
        Bukkit.getScheduler().runTaskLater(this, () -> {
            for (Map.Entry<Block, Material> entry : originalBlocks.entrySet()) {
                entry.getKey().setType(entry.getValue());
            }
        }, 20L * delay);

        // Send feedback to player
        player.sendMessage(ChatColor.YELLOW + "🚪 Mekanisme pintu diaktifkan! Akan pulih dalam " + delay + " detik.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("dungeonreload")) {
            if (!sender.hasPermission("dungeon.reload")) {
                sender.sendMessage(ChatColor.RED + "Kamu tidak memiliki izin untuk menjalankan perintah ini.");
                return true;
            }

            sender.sendMessage(ChatColor.YELLOW + "⟳ Merefresh DungeonMechanism...");

            try {
                this.reloadConfig();
                this.loadSettings();
                this.loadRegionSettings();
                sender.sendMessage(ChatColor.GREEN + "✓ Plugin DungeonMechanism berhasil direload!");
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "❌ Error saat reload: " + e.getMessage());
                getLogger().severe("Error during reload: " + e.getMessage());
            }

            return true;
        }

        return false;
    }

    // Getter methods for other classes
    public ConfigGUI getConfigGUI() {
        return configGUI;
    }

    public String getRegionName() {
        return regionName;
    }

    public int getRadius() {
        return radius;
    }

    public Material getTriggerBlock() {
        return triggerBlock;
    }

    public long getDelay() {
        return delay;
    }
}
