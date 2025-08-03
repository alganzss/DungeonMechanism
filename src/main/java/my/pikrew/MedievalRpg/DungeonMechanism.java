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
// HerbalCraft imports
import my.pikrew.herbalCraft.listeners.GrassClickListener;
import my.pikrew.herbalCraft.listeners.PotionConsumeListener;
import my.pikrew.herbalCraft.managers.ItemManager;
import my.pikrew.herbalCraft.managers.CraftingManager;
import my.pikrew.herbalCraft.managers.BlockManager;
import my.pikrew.herbalCraft.commands.HerbalCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DungeonMechanism extends JavaPlugin implements Listener {

    private static DungeonMechanism instance;
    private WorldGuardPlugin wg;
    private File configFile;
    private FileConfiguration config;
    private ConfigGUI configGUI;

    // HerbalCraft managers
    private ItemManager itemManager;
    private CraftingManager craftingManager;
    private BlockManager blockManager;

    // DungeonChances variables
    private Map<UUID, Integer> playerChances;
    private ScoreboardManager scoreboardManager;

    // DungeonMechanism config
    private String regionName;
    private int radius;
    private Material triggerBlock;
    private long delay;

    // DungeonChances config
    private String dungeonWorldName;
    private String spawnWorldName;
    private int maxChances;
    private String scoreboardTitle;
    private String chanceDisplayName;
    private String dungeonName;
    private String serverInfo;

    @Override
    public void onEnable() {
        instance = this;
        wg = getWorldGuard();

        // Save default config
        saveDefaultConfig();

        // Initialize GUI system
        configGUI = new ConfigGUI(this);

        // Initialize HerbalCraft managers
        itemManager = new ItemManager(this);
        craftingManager = new CraftingManager(this);
        blockManager = new BlockManager(this);

        // Initialize DungeonChances
        playerChances = new HashMap<>();
        scoreboardManager = Bukkit.getScoreboardManager();

        // Register events
        Bukkit.getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(configGUI, this);

        // Register HerbalCraft listeners
        getServer().getPluginManager().registerEvents(new GrassClickListener(this), this);
        getServer().getPluginManager().registerEvents(new PotionConsumeListener(this), this);

        loadSettings();
        loadDungeonChancesConfig();

        // Load data for online players (DungeonChances)
        for (Player player : Bukkit.getOnlinePlayers()) {
            initializePlayer(player);
            updateScoreboard(player);
        }

        getLogger().info("Author:Pikrew");
        getLogger().info("DungeonMechanism enabled");
        getLogger().info("Door Room Dungeon Mechanism enabled");
        getLogger().info("Heal Arena Dungeon Mechanism Enabled");
        getLogger().info("GUI Configuration System enabled");
        getLogger().info("HerbalCraft Plugin has been enabled!");
        getLogger().info("DungeonChances system has been enabled!");

        getServer().getPluginManager().registerEvents(new RegionEntryListener(getLogger()), this);
        getServer().getPluginManager().registerEvents(new Regiontrap(this), this);

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

        // Register HerbalCraft commands
        getCommand("herbal").setExecutor(new HerbalCommand(this));

        getLogger().info("DungeonMechanism plugin telah diaktifkan!");
        getLogger().info("Death Respawn System untuk dungeon telah dimuat!");
        getLogger().info("GUI Configuration System telah dimuat!");
        getLogger().info("Commands registered: /dconfig, /dungeonconfig, /dcfg, /herbal, /dungeonreset, /dungeonreload");
        getLogger().info("Dungeon spawn point: " +
                getConfig().getDouble("dungeon_spawn.x", 183) + ", " +
                getConfig().getDouble("dungeon_spawn.y", 4) + ", " +
                getConfig().getDouble("dungeon_spawn.z", 16));
    }

    @Override
    public void onDisable() {
        // Clear all scoreboards (DungeonChances)
        if (scoreboardManager != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setScoreboard(scoreboardManager.getMainScoreboard());
            }
        }

        getLogger().info("DungeonMechanism plugin telah dinonaktifkan!");
        getLogger().info("GUI Configuration System telah dinonaktifkan!");
        getLogger().info("HerbalCraft Plugin has been disabled!");
        getLogger().info("DungeonChances system has been disabled!");
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

    private void loadDungeonChancesConfig() {
        dungeonWorldName = getConfig().getString("dungeon-world", "DUNGEON");
        spawnWorldName = getConfig().getString("spawn-world", "world");
        maxChances = getConfig().getInt("max-chances", 3);
        scoreboardTitle = ChatColor.translateAlternateColorCodes('&',
                getConfig().getString("scoreboard.title", "&c&lDUNGEON"));
        chanceDisplayName = ChatColor.translateAlternateColorCodes('&',
                getConfig().getString("scoreboard.chance-display", "&eKesempatan: &f"));
        dungeonName = getConfig().getString("scoreboard.dungeon-name", "Wisantara Dungeon");
        serverInfo = getConfig().getString("scoreboard.server-info", "discord.wisantara.com");

        getLogger().info("DungeonChances config loaded:");
        getLogger().info("- Dungeon World: " + dungeonWorldName);
        getLogger().info("- Spawn World: " + spawnWorldName);
        getLogger().info("- Max Chances: " + maxChances);
    }

    private void initializePlayer(Player player) {
        if (!playerChances.containsKey(player.getUniqueId())) {
            playerChances.put(player.getUniqueId(), maxChances);
        }
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

    // ========== EVENT HANDLERS ==========

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        initializePlayer(player);

        // Delay sedikit untuk memastikan player sudah fully loaded
        new BukkitRunnable() {
            @Override
            public void run() {
                updateScoreboard(player);
            }
        }.runTaskLater(this, 5L);
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        updateScoreboard(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Cek apakah player mati di world DUNGEON
        if (!player.getWorld().getName().equals(dungeonWorldName)) {
            return;
        }

        UUID playerId = player.getUniqueId();
        int currentChances = playerChances.getOrDefault(playerId, maxChances);

        if (currentChances > 0) {
            currentChances--;
            playerChances.put(playerId, currentChances);

            // Kirim pesan ke player
            player.sendMessage(ChatColor.RED + "Kamu mati di DUNGEON! Kesempatan tersisa: " +
                    ChatColor.YELLOW + currentChances + ChatColor.RED + "/" + maxChances);

            if (currentChances == 0) {
                player.sendMessage(ChatColor.DARK_RED + "Kesempatan habis! Kamu akan dipindahkan ke spawn.");
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Cek apakah player mati di DUNGEON dan kesempatan habis
        int currentChances = playerChances.getOrDefault(playerId, maxChances);

        new BukkitRunnable() {
            @Override
            public void run() {
                // Cek lagi setelah respawn untuk memastikan
                if (currentChances == 0 && player.getWorld().getName().equals(dungeonWorldName)) {
                    teleportToSpawn(player);
                    // Reset kesempatan
                    playerChances.put(playerId, maxChances);
                }
                updateScoreboard(player);
            }
        }.runTaskLater(this, 1L);
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

    // ========== DUNGEON CHANCES METHODS ==========

    private void teleportToSpawn(Player player) {
        World spawnWorld = Bukkit.getWorld(spawnWorldName);
        if (spawnWorld != null) {
            Location spawnLocation = spawnWorld.getSpawnLocation();
            player.teleport(spawnLocation);
            player.sendMessage(ChatColor.GREEN + "Kamu telah dipindahkan ke spawn. Kesempatan telah direset!");
        } else {
            getLogger().warning("World spawn '" + spawnWorldName + "' tidak ditemukan!");
        }
    }

    private void updateScoreboard(Player player) {
        // Cek apakah player di world DUNGEON
        if (!player.getWorld().getName().equals(dungeonWorldName)) {
            // Hilangkan scoreboard jika tidak di DUNGEON
            player.setScoreboard(scoreboardManager.getMainScoreboard());
            return;
        }

        // Buat scoreboard baru
        Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("dungeonchances", "dummy", scoreboardTitle);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        int chances = playerChances.getOrDefault(player.getUniqueId(), maxChances);

        // Baris 15: Garis atas
        objective.getScore(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬").setScore(15);

        // Baris 14: Kosong
        objective.getScore("§r").setScore(14);

        // Baris 13: Nama dungeon
        objective.getScore(ChatColor.YELLOW + "⚔ " + ChatColor.WHITE + dungeonName).setScore(13);

        // Baris 12: Kosong
        objective.getScore("§r ").setScore(12);

        // Baris 11: Lives dengan hearts
        String hearts = getHeartsDisplay(chances, maxChances);
        objective.getScore(ChatColor.RED + "❤ Lives: " + hearts).setScore(11);

        // Baris 10: Kesempatan numerik
        objective.getScore(ChatColor.GRAY + "   " + chances + "/" + maxChances + " remaining").setScore(10);

        // Baris 9: Kosong
        objective.getScore("§r  ").setScore(9);

        // Baris 8: Status
        String status = chances > 1 ? ChatColor.GREEN + "✓ Safe" :
                chances == 1 ? ChatColor.YELLOW + "⚠ Warning" :
                        ChatColor.RED + "✗ Critical";
        objective.getScore(status).setScore(8);

        // Baris 7: Kosong
        objective.getScore("§r   ").setScore(7);

        // Baris 6: Tips
        if (chances <= 1) {
            objective.getScore(ChatColor.RED + "⚡ Be careful!").setScore(6);
        } else {
            objective.getScore(ChatColor.AQUA + "⭐ Good luck!").setScore(6);
        }

        // Baris 5: Kosong
        objective.getScore("§r    ").setScore(5);

        // Baris 4: Server info
        objective.getScore(ChatColor.GRAY + serverInfo).setScore(4);

        // Baris 3: Kosong
        objective.getScore("§r     ").setScore(3);

        // Baris 2: Garis bawah
        objective.getScore(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬").setScore(2);

        // Set scoreboard ke player
        player.setScoreboard(scoreboard);
    }

    private String getHeartsDisplay(int current, int max) {
        StringBuilder hearts = new StringBuilder();

        // Hearts yang tersisa (merah)
        for (int i = 0; i < current; i++) {
            hearts.append(ChatColor.RED).append("♥");
        }

        // Hearts yang hilang (abu-abu)
        for (int i = current; i < max; i++) {
            hearts.append(ChatColor.GRAY).append("♥");
        }

        return hearts.toString();
    }

    // ========== COMMANDS ==========

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
                this.loadDungeonChancesConfig();
                this.loadRegionSettings();

                // Update semua scoreboard
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateScoreboard(player);
                }

                sender.sendMessage(ChatColor.GREEN + "✓ Plugin DungeonMechanism berhasil direload!");
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "❌ Error saat reload: " + e.getMessage());
                getLogger().severe("Error during reload: " + e.getMessage());
            }

            return true;
        }

        if (command.getName().equalsIgnoreCase("dungeonreset")) {
            if (!sender.hasPermission("dungeonchances.reset")) {
                sender.sendMessage(ChatColor.RED + "Kamu tidak memiliki permission!");
                return true;
            }

            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /dungeonreset <player>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player tidak ditemukan!");
                return true;
            }

            playerChances.put(target.getUniqueId(), maxChances);
            updateScoreboard(target);
            sender.sendMessage(ChatColor.GREEN + "Kesempatan " + target.getName() + " telah direset!");
            target.sendMessage(ChatColor.GREEN + "Kesempatan dungeon kamu telah direset oleh admin!");

            return true;
        }

        return false;
    }

    // ========== GETTER METHODS ==========

    // DungeonMechanism getters
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

    // Static instance getter and HerbalCraft manager getters
    public static DungeonMechanism getInstance() {
        return instance;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public CraftingManager getCraftingManager() {
        return craftingManager;
    }

    public BlockManager getBlockManager() {
        return blockManager;
    }

    // DungeonChances getters
    public Map<UUID, Integer> getPlayerChances() {
        return playerChances;
    }

    public String getDungeonWorldName() {
        return dungeonWorldName;
    }

    public String getSpawnWorldName() {
        return spawnWorldName;
    }

    public int getMaxChances() {
        return maxChances;
    }
}