package my.pikrew.MedievalRpg;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
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
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

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

    // CheckPoint variables
    private Map<UUID, Integer> playerCheckpoints = new HashMap<>();
    private List<Location> checkpointLocations = new ArrayList<>();
    private Set<UUID> debugPlayers = new HashSet<>();
    private File checkpointDataFile;
    private FileConfiguration checkpointDataConfig;

    // RegionBlockRemover variables
    private Map<String, RegionRemovalTask> activeTasks = new HashMap<>();

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

        // Initialize CheckPoint system
        setupCheckpointDataFile();
        loadCheckpoints();
        loadPlayerCheckpointData();

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

        // Start checkpoint visual effects
        startCheckpointVisualEffectsTask();

        // Set command executors
        this.getCommand("removeregion").setExecutor(this);
        getCommand("dungeonspawn").setExecutor(new DeathEventCommand(this));

        // Register GUI command
        ConfigGUICommand guiCommand = new ConfigGUICommand(this, configGUI);
        getCommand("dconfig").setExecutor(guiCommand);
        getCommand("dconfig").setTabCompleter(guiCommand);

        // Register HerbalCraft commands
        getCommand("herbal").setExecutor(new HerbalCommand(this));

        getLogger().info("Author:Pikrew");
        getLogger().info("DungeonMechanism enabled");
        getLogger().info("RegionBlockRemover integrated successfully!");
        getLogger().info("Door Room Dungeon Mechanism enabled");
        getLogger().info("Heal Arena Dungeon Mechanism Enabled");
        getLogger().info("GUI Configuration System enabled");
        getLogger().info("HerbalCraft Plugin has been enabled!");
        getLogger().info("DungeonChances system has been enabled!");
        getLogger().info("CheckPoint system has been enabled!");

        getServer().getPluginManager().registerEvents(new RegionEntryListener(getLogger()), this);
        getServer().getPluginManager().registerEvents(new Regiontrap(this), this);

        // Register existing listeners
        getServer().getPluginManager().registerEvents(new Regiontrap(this), this);
        // Register death respawn listener
        getServer().getPluginManager().registerEvents(new DeathEvent(this), this);

        getLogger().info("DungeonMechanism plugin telah diaktifkan!");
        getLogger().info("Death Respawn System untuk dungeon telah dimuat!");
        getLogger().info("GUI Configuration System telah dimuat!");
        getLogger().info("Commands registered: /dconfig, /dungeonconfig, /dcfg, /herbal, /dungeonreset, /dungeonreload, /checkpoint, /removeregion");
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

        // Save checkpoint data before shutdown
        savePlayerCheckpointData();
        saveCheckpoints();

        // Stop all active RegionBlockRemover tasks
        for (RegionRemovalTask task : activeTasks.values()) {
            task.cancel();
        }
        activeTasks.clear();

        getLogger().info("DungeonMechanism plugin telah dinonaktifkan!");
        getLogger().info("RegionBlockRemover tasks stopped!");
        getLogger().info("GUI Configuration System telah dinonaktifkan!");
        getLogger().info("HerbalCraft Plugin has been disabled!");
        getLogger().info("DungeonChances system has been disabled!");
        getLogger().info("CheckPoint system has been disabled!");
    }

    // ========== REGION BLOCK REMOVER METHODS ==========

    private void removeRegionBlocks(CommandSender sender, World world, String regionName) {
        // Cek apakah region sedang dalam proses
        String taskKey = world.getName() + "_" + regionName;
        if (activeTasks.containsKey(taskKey)) {
            sender.sendMessage("§cRegion " + regionName + " di world " + world.getName() + " sedang dalam proses removal!");
            return;
        }

        // Dapatkan WorldGuard RegionManager
        RegionManager regionManager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(world));

        if (regionManager == null) {
            sender.sendMessage("§cTidak dapat mengakses RegionManager untuk world " + world.getName() + "!");
            return;
        }

        // Cari region
        ProtectedRegion region = regionManager.getRegion(regionName);
        if (region == null) {
            sender.sendMessage("§cRegion '" + regionName + "' tidak ditemukan di world " + world.getName() + "!");
            return;
        }

        // Dapatkan boundaries region
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        // Log informasi untuk console
        if (sender instanceof ConsoleCommandSender) {
            getLogger().info("Memulai penghapusan region '" + regionName + "' di world '" + world.getName() + "'");
            getLogger().info("Koordinat: (" + min.getBlockX() + "," + min.getBlockY() + "," + min.getBlockZ() + ") sampai (" +
                    max.getBlockX() + "," + max.getBlockY() + "," + max.getBlockZ() + ")");
        }

        // Buat dan jalankan task penghapusan
        RegionRemovalTask task = new RegionRemovalTask(world, min, max, regionName, sender);
        activeTasks.put(taskKey, task);
        task.runTaskTimer(this, 0L, 10L); // Jalankan setiap 0.5 detik
    }

    private class RegionRemovalTask extends BukkitRunnable {
        private final World world;
        private final BlockVector3 min;
        private final BlockVector3 max;
        private final String regionName;
        private final CommandSender commandSender;
        private final List<SavedBlockData> blocksToRemove;
        private final List<SavedBlockData> removedBlocks;
        private int currentIndex = 0;
        private final String taskKey;

        public RegionRemovalTask(World world, BlockVector3 min, BlockVector3 max, String regionName, CommandSender commandSender) {
            this.world = world;
            this.min = min;
            this.max = max;
            this.regionName = regionName;
            this.commandSender = commandSender;
            this.blocksToRemove = new ArrayList<>();
            this.removedBlocks = new ArrayList<>();
            this.taskKey = world.getName() + "_" + regionName;

            // Kumpulkan semua blok dalam region (dari bawah ke atas, 2 layer sekaligus)
            collectBlocks();
        }

        private void collectBlocks() {
            // Kumpulkan blok per layer (2 layer sekaligus)
            for (int y = min.getBlockY(); y <= max.getBlockY(); y += 2) {
                List<SavedBlockData> layerBlocks = new ArrayList<>();

                // Layer pertama
                for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                    for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                        Block block = world.getBlockAt(x, y, z);
                        if (block.getType() != Material.AIR) {
                            layerBlocks.add(new SavedBlockData(block.getLocation(), block.getType(), block.getBlockData()));
                        }
                    }
                }

                // Layer kedua (jika ada)
                if (y + 1 <= max.getBlockY()) {
                    for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                        for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                            Block block = world.getBlockAt(x, y + 1, z);
                            if (block.getType() != Material.AIR) {
                                layerBlocks.add(new SavedBlockData(block.getLocation(), block.getType(), block.getBlockData()));
                            }
                        }
                    }
                }

                blocksToRemove.addAll(layerBlocks);
            }

            // Log total blok yang akan dihapus
            if (commandSender instanceof ConsoleCommandSender) {
                getLogger().info("Total " + blocksToRemove.size() + " blok akan dihapus dari region '" + regionName + "'");
            }
        }

        @Override
        public void run() {
            if (currentIndex >= blocksToRemove.size()) {
                // Semua blok telah dihapus, mulai regenerasi
                startRegeneration();
                return;
            }

            // Hapus 2 layer blok sekaligus
            int layerStartY = min.getBlockY() + (currentIndex / ((max.getBlockX() - min.getBlockX() + 1) * (max.getBlockZ() - min.getBlockZ() + 1) * 2)) * 2;
            List<SavedBlockData> currentLayerBlocks = new ArrayList<>();

            // Kumpulkan blok dari 2 layer saat ini
            while (currentIndex < blocksToRemove.size()) {
                SavedBlockData blockData = blocksToRemove.get(currentIndex);
                int blockY = blockData.location.getBlockY();

                if (blockY >= layerStartY && blockY < layerStartY + 2) {
                    currentLayerBlocks.add(blockData);
                    currentIndex++;
                } else {
                    break;
                }
            }

            // Hapus blok dan buat efek
            for (SavedBlockData blockData : currentLayerBlocks) {
                Block block = blockData.location.getBlock();
                if (block.getType() != Material.AIR) {
                    // Simpan data blok untuk regenerasi
                    removedBlocks.add(blockData);

                    // Hapus blok
                    block.setType(Material.AIR);

                    // Efek ledakan
                    Location loc = blockData.location.clone().add(0.5, 0.5, 0.5);
                    world.spawnParticle(Particle.EXPLOSION, loc, 3, 0.2, 0.2, 0.2, 0.1);
                    world.spawnParticle(Particle.SMOKE, loc, 2, 0.1, 0.1, 0.1, 0.05);

                    // Suara ledakan (lebih pelan)
                    world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.3f, 1.2f);
                }
            }
        }

        private void startRegeneration() {
            // Log untuk console
            if (commandSender instanceof ConsoleCommandSender) {
                getLogger().info("Penghapusan blok selesai untuk region '" + regionName + "'. Memulai regenerasi dalam 6 detik...");
            }

            // Regenerasi setelah 6 detik
            Bukkit.getScheduler().runTaskLater(DungeonMechanism.this, new Runnable() {
                @Override
                public void run() {
                    regenerateBlocks();
                }
            }, 12000L); // 120 ticks = 6 detik

            // Hapus dari active tasks
            activeTasks.remove(taskKey);
            this.cancel();
        }

        private void regenerateBlocks() {
            // Log untuk console
            if (commandSender instanceof ConsoleCommandSender) {
                getLogger().info("Memulai regenerasi " + removedBlocks.size() + " blok untuk region '" + regionName + "'");
            }

            // Regenerasi blok secara bertahap
            new BukkitRunnable() {
                int index = 0;

                @Override
                public void run() {
                    if (index >= removedBlocks.size()) {
                        // Log completion untuk console
                        if (commandSender instanceof ConsoleCommandSender) {
                            getLogger().info("Regenerasi blok selesai untuk region '" + regionName + "' di world '" + world.getName() + "'!");
                        }
                        this.cancel();
                        return;
                    }

                    // Regenerasi beberapa blok sekaligus
                    int endIndex = Math.min(index + 10, removedBlocks.size());

                    for (int i = index; i < endIndex; i++) {
                        SavedBlockData blockData = removedBlocks.get(i);
                        Block block = blockData.location.getBlock();

                        // Kembalikan blok
                        block.setType(blockData.material);
                        block.setBlockData(blockData.blockData);

                        // Efek regenerasi
                        Location loc = blockData.location.clone().add(0.5, 0.5, 0.5);
                        world.spawnParticle(Particle.HAPPY_VILLAGER, loc, 5, 0.3, 0.3, 0.3, 0);
                        world.spawnParticle(Particle.END_ROD, loc, 2, 0.2, 0.2, 0.2, 0.1);
                    }

                    // Suara regenerasi
                    if (endIndex > index) {
                        Location centerLoc = new Location(world,
                                (min.getBlockX() + max.getBlockX()) / 2.0,
                                (min.getBlockY() + max.getBlockY()) / 2.0,
                                (min.getBlockZ() + max.getBlockZ()) / 2.0);
                        world.playSound(centerLoc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.5f, 1.5f);
                    }

                    // Progress log untuk console setiap 50 blok
                    if (commandSender instanceof ConsoleCommandSender && (endIndex - index) > 0 && endIndex % 50 < 10) {
                        getLogger().info("Progress regenerasi: " + endIndex + "/" + removedBlocks.size() + " blok (" +
                                String.format("%.1f", (double)endIndex / removedBlocks.size() * 100) + "%)");
                    }

                    index = endIndex;
                }
            }.runTaskTimer(DungeonMechanism.this, 0L, 2L); // Setiap 0.1 detik
        }
    }

    private static class SavedBlockData {
        final Location location;
        final Material material;
        final BlockData blockData;

        public SavedBlockData(Location location, Material material, BlockData blockData) {
            this.location = location;
            this.material = material;
            this.blockData = blockData;
        }
    }

    // ========== CHECKPOINT SYSTEM METHODS ==========

    private void setupCheckpointDataFile() {
        checkpointDataFile = new File(getDataFolder(), "checkpoint_data.yml");
        if (!checkpointDataFile.exists()) {
            checkpointDataFile.getParentFile().mkdirs();
            try {
                checkpointDataFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Could not create checkpoint data file: " + e.getMessage());
            }
        }
        checkpointDataConfig = YamlConfiguration.loadConfiguration(checkpointDataFile);
    }

    private void loadCheckpoints() {
        if (checkpointDataConfig.contains("checkpoints")) {
            List<Map<?, ?>> checkpointList = checkpointDataConfig.getMapList("checkpoints");
            for (Map<?, ?> checkpoint : checkpointList) {
                String worldName = (String) checkpoint.get("world");
                double x = (Double) checkpoint.get("x");
                double y = (Double) checkpoint.get("y");
                double z = (Double) checkpoint.get("z");
                float yaw = ((Double) checkpoint.get("yaw")).floatValue();
                float pitch = ((Double) checkpoint.get("pitch")).floatValue();

                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    Location loc = new Location(world, x, y, z, yaw, pitch);
                    checkpointLocations.add(loc);
                }
            }
        }
    }

    private void saveCheckpoints() {
        List<Map<String, Object>> checkpointList = new ArrayList<>();
        for (Location loc : checkpointLocations) {
            Map<String, Object> checkpoint = new HashMap<>();
            checkpoint.put("world", loc.getWorld().getName());
            checkpoint.put("x", loc.getX());
            checkpoint.put("y", loc.getY());
            checkpoint.put("z", loc.getZ());
            checkpoint.put("yaw", (double) loc.getYaw());
            checkpoint.put("pitch", (double) loc.getPitch());
            checkpointList.add(checkpoint);
        }
        checkpointDataConfig.set("checkpoints", checkpointList);

        try {
            checkpointDataConfig.save(checkpointDataFile);
        } catch (IOException e) {
            getLogger().severe("Tidak dapat menyimpan checkpoint: " + e.getMessage());
        }
    }

    private void loadPlayerCheckpointData() {
        if (checkpointDataConfig.contains("players")) {
            for (String uuidString : checkpointDataConfig.getConfigurationSection("players").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                int checkpoint = checkpointDataConfig.getInt("players." + uuidString + ".checkpoint");
                playerCheckpoints.put(uuid, checkpoint);
            }
        }
    }

    private void savePlayerCheckpointData() {
        for (Map.Entry<UUID, Integer> entry : playerCheckpoints.entrySet()) {
            checkpointDataConfig.set("players." + entry.getKey().toString() + ".checkpoint", entry.getValue());
        }

        try {
            checkpointDataConfig.save(checkpointDataFile);
        } catch (IOException e) {
            getLogger().severe("Tidak dapat menyimpan data player checkpoint: " + e.getMessage());
        }
    }

    private void spawnCheckpointEffects(Location loc) {
        // Efek partikel
        loc.getWorld().spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER,
                loc.clone().add(0, 1, 0), 30, 1, 1, 1, 0.1);

        // Efek suara
        loc.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }

    private void startCheckpointVisualEffectsTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Location loc : checkpointLocations) {
                    if (loc.getWorld() != null) {
                        // Efek partikel ringan untuk menandai checkpoint
                        loc.getWorld().spawnParticle(org.bukkit.Particle.END_ROD,
                                loc.clone().add(0, 2, 0), 5, 0.5, 0.5, 0.5, 0.05);
                    }
                }
            }
        }.runTaskTimer(this, 0L, 40L); // Setiap 2 detik
    }

    // ========== EXISTING METHODS ==========

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
        UUID playerId = player.getUniqueId();

        // Initialize DungeonChances
        initializePlayer(player);

        // Handle CheckPoint system
        if (!playerCheckpoints.containsKey(playerId)) {
            playerCheckpoints.put(playerId, 0); // Set ke checkpoint awal (index 0)

            if (!checkpointLocations.isEmpty()) {
                player.teleport(checkpointLocations.get(0));
                player.sendMessage("§a[Checkpoint] Selamat datang! Anda telah ditempatkan di checkpoint awal.");
            }
        } else {
            // Teleport ke checkpoint terakhir
            int currentCheckpoint = playerCheckpoints.get(playerId);
            if (currentCheckpoint < checkpointLocations.size()) {
                player.teleport(checkpointLocations.get(currentCheckpoint));
                player.sendMessage("§a[Checkpoint] Selamat datang kembali! Checkpoint: " + (currentCheckpoint + 1));
            }
        }

        // Delay sedikit untuk memastikan player sudah fully loaded
        new BukkitRunnable() {
            @Override
            public void run() {
                updateScoreboard(player);
            }
        }.runTaskLater(this, 5L);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Location playerLoc = player.getLocation();

        // Skip jika player belum terdaftar
        if (!playerCheckpoints.containsKey(playerId)) {
            return;
        }

        // Skip jika tidak ada checkpoint
        if (checkpointLocations.isEmpty()) {
            return;
        }

        int currentCheckpoint = playerCheckpoints.get(playerId);
        int nextCheckpoint = currentCheckpoint + 1;

        // Debug: Tampilkan info untuk debug players
        if (debugPlayers.contains(playerId)) {
            player.sendMessage("§7[Debug] Current checkpoint: " + (currentCheckpoint + 1) + "/" + checkpointLocations.size());

            if (nextCheckpoint < checkpointLocations.size()) {
                Location nextCheckpointLoc = checkpointLocations.get(nextCheckpoint);
                if (playerLoc.getWorld() != null &&
                        nextCheckpointLoc.getWorld() != null &&
                        playerLoc.getWorld().equals(nextCheckpointLoc.getWorld())) {

                    double distance = playerLoc.distance(nextCheckpointLoc);
                    player.sendMessage("§7[Debug] Distance to next checkpoint: " + String.format("%.2f", distance));
                }
            }
        }

        // Cek apakah player mencapai checkpoint berikutnya
        if (nextCheckpoint < checkpointLocations.size()) {
            Location nextCheckpointLoc = checkpointLocations.get(nextCheckpoint);

            // Pastikan world sama dan hitung jarak
            if (playerLoc.getWorld() != null &&
                    nextCheckpointLoc.getWorld() != null &&
                    playerLoc.getWorld().equals(nextCheckpointLoc.getWorld())) {

                double distance = playerLoc.distance(nextCheckpointLoc);

                // Debug distance (tampilkan untuk debug players yang sneak)
                if (debugPlayers.contains(playerId) && player.isSneaking()) {
                    player.sendMessage("§7[Debug] Distance to next checkpoint: " + String.format("%.2f", distance));
                    player.sendMessage("§7[Debug] Next checkpoint: X:" + (int) nextCheckpointLoc.getX() +
                            " Y:" + (int) nextCheckpointLoc.getY() + " Z:" + (int) nextCheckpointLoc.getZ());
                }

                // Cek jarak (radius 5 blok, diperbesar untuk memudahkan)
                if (distance <= 5.0) {
                    // Update checkpoint player
                    playerCheckpoints.put(playerId, nextCheckpoint);

                    // Kirim pesan
                    player.sendMessage("§e[Checkpoint] Checkpoint " + (nextCheckpoint + 1) + " tercapai!");
                    player.sendMessage("§a[Checkpoint] Checkpoint sebelumnya telah dihapus.");
                    player.sendMessage("§7[Info] Jarak: " + String.format("%.2f", distance) + " blok");

                    // Efek visual
                    spawnCheckpointEffects(nextCheckpointLoc);

                    // Auto save
                    savePlayerCheckpointData();
                }
            }
        } else {
            // Player sudah di checkpoint terakhir
            if (debugPlayers.contains(playerId) && player.isSneaking()) {
                player.sendMessage("§a[Debug] Anda sudah di checkpoint terakhir!");
            }
        }
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

        // Handle CheckPoint respawn
        if (playerCheckpoints.containsKey(playerId)) {
            int currentCheckpoint = playerCheckpoints.get(playerId);
            if (currentCheckpoint < checkpointLocations.size()) {
                event.setRespawnLocation(checkpointLocations.get(currentCheckpoint));

                // Delay message untuk memastikan player sudah spawn
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.sendMessage("§c[Checkpoint] Anda telah respawn di checkpoint " + (currentCheckpoint + 1));
                    }
                }.runTaskLater(this, 5L);
            }
        }

        // Handle DungeonChances respawn
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
        // RegionBlockRemover command
        if (command.getName().equalsIgnoreCase("removeregion")) {
            // Cek format command
            if (args.length == 1) {
                // Format: /removeregion <region_name>
                // Jika dari player, gunakan world player
                // Jika dari console, gunakan world default
                String regionName = args[0];
                World world = null;

                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    world = player.getWorld();
                } else {
                    // Dari console, gunakan world default (biasanya "world")
                    world = Bukkit.getWorld("world");
                    if (world == null && !Bukkit.getWorlds().isEmpty()) {
                        world = Bukkit.getWorlds().get(0); // Ambil world pertama
                    }
                }

                if (world == null) {
                    sender.sendMessage("§cTidak dapat menemukan world yang valid!");
                    return true;
                }

                removeRegionBlocks(sender, world, regionName);
                return true;

            } else if (args.length == 2) {
                // Format: /removeregion <world_name> <region_name>
                String worldName = args[0];
                String regionName = args[1];

                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    sender.sendMessage("§cWorld '" + worldName + "' tidak ditemukan!");
                    return true;
                }

                removeRegionBlocks(sender, world, regionName);
                return true;

            } else {
                // Format salah
                sender.sendMessage("§cPenggunaan:");
                sender.sendMessage("§c/removeregion <nama_region> - Hapus region di world saat ini (player) atau world default (console)");
                sender.sendMessage("§c/removeregion <nama_world> <nama_region> - Hapus region di world tertentu");
                return true;
            }
        }

        // Existing commands
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
                this.loadCheckpoints();
                this.loadPlayerCheckpointData();

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

        // CheckPoint commands
        if (command.getName().equalsIgnoreCase("checkpoint")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cCommand ini hanya dapat digunakan oleh player!");
                return true;
            }

            Player player = (Player) sender;

            if (args.length == 0) {
                player.sendMessage("§e=== Checkpoint Commands ===");
                player.sendMessage("§a/checkpoint add §7- Tambah checkpoint di lokasi Anda");
                player.sendMessage("§a/checkpoint list §7- Lihat semua checkpoint");
                player.sendMessage("§a/checkpoint remove <nomor> §7- Hapus checkpoint");
                player.sendMessage("§a/checkpoint tp <nomor> §7- Teleport ke checkpoint");
                player.sendMessage("§a/checkpoint reset <player> §7- Reset checkpoint player");
                player.sendMessage("§a/checkpoint info §7- Lihat info checkpoint Anda");
                player.sendMessage("§a/checkpoint debug §7- Toggle debug mode");
                player.sendMessage("§a/checkpoint reload §7- Reload checkpoint data");
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "add":
                    if (!player.hasPermission("checkpoint.admin")) {
                        player.sendMessage("§cAnda tidak memiliki permission!");
                        return true;
                    }

                    Location loc = player.getLocation();
                    checkpointLocations.add(loc);
                    saveCheckpoints();
                    player.sendMessage("§a[Checkpoint] Checkpoint baru ditambahkan! Total: " + checkpointLocations.size());
                    break;

                case "list":
                    if (!player.hasPermission("checkpoint.admin")) {
                        player.sendMessage("§cAnda tidak memiliki permission!");
                        return true;
                    }

                    player.sendMessage("§e=== Daftar Checkpoint ===");
                    for (int i = 0; i < checkpointLocations.size(); i++) {
                        Location checkLoc = checkpointLocations.get(i);
                        player.sendMessage("§a" + (i + 1) + ". §7World: " + checkLoc.getWorld().getName() +
                                " X: " + (int) checkLoc.getX() + " Y: " + (int) checkLoc.getY() + " Z: " + (int) checkLoc.getZ());
                    }
                    break;

                case "debug":
                    // Toggle debug mode untuk player
                    if (debugPlayers.contains(player.getUniqueId())) {
                        debugPlayers.remove(player.getUniqueId());
                        player.sendMessage("§c[Checkpoint] Debug mode dinonaktifkan");
                    } else {
                        debugPlayers.add(player.getUniqueId());
                        player.sendMessage("§a[Checkpoint] Debug mode diaktifkan");
                        player.sendMessage("§7Sneak untuk melihat info debug detail");
                    }
                    break;

                case "reload":
                    if (!player.hasPermission("checkpoint.admin")) {
                        player.sendMessage("§cAnda tidak memiliki permission!");
                        return true;
                    }

                    loadCheckpoints();
                    loadPlayerCheckpointData();
                    player.sendMessage("§a[Checkpoint] Data berhasil di-reload!");
                    break;

                case "remove":
                    if (!player.hasPermission("checkpoint.admin")) {
                        player.sendMessage("§cAnda tidak memiliki permission!");
                        return true;
                    }

                    if (args.length < 2) {
                        player.sendMessage("§c/checkpoint remove <nomor>");
                        return true;
                    }

                    try {
                        int index = Integer.parseInt(args[1]) - 1;
                        if (index >= 0 && index < checkpointLocations.size()) {
                            checkpointLocations.remove(index);
                            saveCheckpoints();
                            player.sendMessage("§a[Checkpoint] Checkpoint berhasil dihapus!");
                        } else {
                            player.sendMessage("§cNomor checkpoint tidak valid!");
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cNomor tidak valid!");
                    }
                    break;

                case "tp":
                    if (!player.hasPermission("checkpoint.admin")) {
                        player.sendMessage("§cAnda tidak memiliki permission!");
                        return true;
                    }

                    if (args.length < 2) {
                        player.sendMessage("§c/checkpoint tp <nomor>");
                        return true;
                    }

                    try {
                        int index = Integer.parseInt(args[1]) - 1;
                        if (index >= 0 && index < checkpointLocations.size()) {
                            player.teleport(checkpointLocations.get(index));
                            player.sendMessage("§a[Checkpoint] Teleport ke checkpoint " + (index + 1));
                        } else {
                            player.sendMessage("§cNomor checkpoint tidak valid!");
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cNomor tidak valid!");
                    }
                    break;

                case "reset":
                    if (!player.hasPermission("checkpoint.admin")) {
                        player.sendMessage("§cAnda tidak memiliki permission!");
                        return true;
                    }

                    if (args.length < 2) {
                        player.sendMessage("§c/checkpoint reset <player>");
                        return true;
                    }

                    Player target = Bukkit.getPlayer(args[1]);
                    if (target != null) {
                        playerCheckpoints.put(target.getUniqueId(), 0);
                        savePlayerCheckpointData();
                        player.sendMessage("§a[Checkpoint] Checkpoint " + target.getName() + " telah direset!");
                        target.sendMessage("§e[Checkpoint] Checkpoint Anda telah direset oleh admin!");
                    } else {
                        player.sendMessage("§cPlayer tidak ditemukan!");
                    }
                    break;

                case "info":
                    UUID playerId = player.getUniqueId();
                    if (playerCheckpoints.containsKey(playerId)) {
                        int currentCheckpoint = playerCheckpoints.get(playerId);
                        player.sendMessage("§e[Checkpoint] Checkpoint saat ini: " + (currentCheckpoint + 1) + "/" + checkpointLocations.size());

                        if (currentCheckpoint + 1 < checkpointLocations.size()) {
                            Location nextLoc = checkpointLocations.get(currentCheckpoint + 1);
                            if (player.getLocation().getWorld().equals(nextLoc.getWorld())) {
                                double distance = player.getLocation().distance(nextLoc);
                                player.sendMessage("§7Jarak ke checkpoint berikutnya: " + String.format("%.1f", distance) + " blok");
                                player.sendMessage("§7Koordinat checkpoint berikutnya: X:" + (int) nextLoc.getX() +
                                        " Y:" + (int) nextLoc.getY() + " Z:" + (int) nextLoc.getZ());
                            }
                        } else {
                            player.sendMessage("§a[Checkpoint] Anda telah mencapai checkpoint terakhir!");
                        }

                        // Tampilkan koordinat player saat ini
                        Location pLoc = player.getLocation();
                        player.sendMessage("§7Koordinat Anda: X:" + (int) pLoc.getX() +
                                " Y:" + (int) pLoc.getY() + " Z:" + (int) pLoc.getZ());
                    } else {
                        player.sendMessage("§c[Checkpoint] Data checkpoint tidak ditemukan!");
                    }
                    break;

                default:
                    player.sendMessage("§cCommand tidak dikenal! Gunakan /checkpoint untuk bantuan.");
                    break;
            }
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

    // CheckPoint getters
    public Map<UUID, Integer> getPlayerCheckpoints() {
        return playerCheckpoints;
    }

    public List<Location> getCheckpointLocations() {
        return checkpointLocations;
    }

    public Set<UUID> getDebugPlayers() {
        return debugPlayers;
    }

    // RegionBlockRemover getters
    public Map<String, RegionRemovalTask> getActiveTasks() {
        return activeTasks;
    }
}