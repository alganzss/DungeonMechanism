package my.pikrew.herbalCraft.managers;

import my.pikrew.MedievalRpg.DungeonMechanism;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class BlockManager {

    private final DungeonMechanism plugin;
    private final Map<Location, BlockData> regeneratingBlocks;

    public BlockManager(DungeonMechanism plugin) {
        this.plugin = plugin;
        this.regeneratingBlocks = new HashMap<>();
    }

    public void handleBlockBreak(Block block) {
        Location loc = block.getLocation();
        Material originalType = block.getType();

        // Store original block data
        BlockData blockData = new BlockData(originalType, System.currentTimeMillis());
        regeneratingBlocks.put(loc, blockData);

        // Set block to air
        block.setType(Material.AIR);

        // Play break sound
        block.getWorld().playSound(loc, Sound.BLOCK_GRASS_BREAK, 1.0f, 0.8f);

        // Spawn particles
        block.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE,
                loc.add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, originalType.createBlockData());

        // Schedule block regeneration
        scheduleBlockRegeneration(loc, originalType);
    }

    private void scheduleBlockRegeneration(Location location, Material originalType) {
        int regenerationTime = plugin.getConfig().getInt("general.block-regeneration-time", 300); // 5 minutes default

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                regenerateBlock(location, originalType);
            }
        }.runTaskLater(plugin, regenerationTime * 20L); // Convert seconds to ticks

        // Store task for potential cancellation - FIX: Check if location exists in map
        BlockData blockData = regeneratingBlocks.get(location);
        if (blockData != null) {
            blockData.setTask(task);
        }
    }

    private void regenerateBlock(Location location, Material originalType) {
        Block block = location.getBlock();

        // Only regenerate if block is still air
        if (block.getType() == Material.AIR) {
            block.setType(originalType);

            // Play regeneration sound
            block.getWorld().playSound(location, Sound.BLOCK_GRASS_PLACE, 1.0f, 1.2f);

            // Spawn regeneration particles
            block.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                    location.add(0.5, 1, 0.5), 15, 0.5, 0.5, 0.5, 0.1);

            // Spawn some extra sparkle particles
            block.getWorld().spawnParticle(Particle.END_ROD,
                    location, 5, 0.3, 0.3, 0.3, 0.05);
        }

        // Remove from tracking
        regeneratingBlocks.remove(location);
    }

    public boolean isBlockRegenerating(Location location) {
        return regeneratingBlocks.containsKey(location);
    }

    public long getRegenerationTime(Location location) {
        BlockData data = regeneratingBlocks.get(location);
        if (data == null) return 0;

        int regenTime = plugin.getConfig().getInt("general.block-regeneration-time", 300);
        long elapsedTime = (System.currentTimeMillis() - data.getBreakTime()) / 1000;
        return Math.max(0, regenTime - elapsedTime);
    }

    public void cancelAllRegeneration() {
        for (BlockData data : regeneratingBlocks.values()) {
            if (data.getTask() != null) {
                data.getTask().cancel();
            }
        }
        regeneratingBlocks.clear();
    }

    // Inner class to store block data
    private static class BlockData {
        private final Material originalType;
        private final long breakTime;
        private BukkitTask task;

        public BlockData(Material originalType, long breakTime) {
            this.originalType = originalType;
            this.breakTime = breakTime;
        }

        public Material getOriginalType() {
            return originalType;
        }

        public long getBreakTime() {
            return breakTime;
        }

        public BukkitTask getTask() {
            return task;
        }

        public void setTask(BukkitTask task) {
            this.task = task;
        }
    }
}