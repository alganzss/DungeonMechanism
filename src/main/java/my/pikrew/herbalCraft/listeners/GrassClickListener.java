package my.pikrew.herbalCraft.listeners;

import my.pikrew.MedievalRpg.DungeonMechanism;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.file.FileConfiguration;
import my.pikrew.herbalCraft.enums.HerbalTier;
import my.pikrew.herbalCraft.utils.MessageUtils;

import java.util.Random;

public class GrassClickListener implements Listener {

    private final DungeonMechanism plugin;
    private final Random random;

    public GrassClickListener(DungeonMechanism plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Player player = event.getPlayer();
        World world = player.getWorld();

        // Check if it's a dungeon world
        FileConfiguration config = plugin.getConfig();
        if (!config.getStringList("dungeon-worlds").contains(world.getName())) {
            return;
        }

        // Check if block is grass
        if (block.getType() != Material.SHORT_GRASS &&
                block.getType() != Material.SHORT_GRASS &&
                block.getType() != Material.SHORT_GRASS) {
            return;
        }

        // Check drop chance
        double dropChance = config.getDouble("general.drop-chance", 0.3);
        if (random.nextDouble() > dropChance) {
            return;
        }

        // Determine tier based on chance
        HerbalTier tier = determineTier(config);

        // Get herbal item
        ItemStack herbalItem = plugin.getItemManager().getHerbalItem(tier);

        // Drop item
        world.dropItemNaturally(block.getLocation().add(0, 1, 0), herbalItem);

        // Handle block breaking and regeneration
        plugin.getBlockManager().handleBlockBreak(block);

        // Send message
        String message = config.getString("messages.herbal-found")
                .replace("{tier}", tier.getDisplayName())
                .replace("{item}", herbalItem.getItemMeta().getDisplayName());
        MessageUtils.sendMessage(player, message);

        // Special handling for legendary drops
        if (tier == HerbalTier.LEGENDARY) {
            // Log untuk debugging
            plugin.getLogger().info("Legendary drop detected for player: " + player.getName());
            sendLegendaryBroadcast(player, herbalItem);
        }

        event.setCancelled(true);
    }

    private HerbalTier determineTier(FileConfiguration config) {
        double rand = random.nextDouble() * 100;
        double current = 0;

        for (HerbalTier tier : HerbalTier.values()) {
            current += config.getDouble("tier-chances." + tier.name().toLowerCase(), tier.getDefaultChance());
            if (rand <= current) {
                return tier;
            }
        }

        return HerbalTier.COMMON;
    }

    private void sendLegendaryBroadcast(Player player, ItemStack item) {
        try {
            FileConfiguration config = plugin.getConfig();
            String broadcastMessage = config.getString("messages.legendary-broadcast",
                            "&6&l[LEGENDARY DROP!] &e{player} &7found a {item} &7in the dungeon! &6✨")
                    .replace("{player}", player.getName())
                    .replace("{item}", item.getItemMeta().getDisplayName());

            // Log untuk debugging
            plugin.getLogger().info("Broadcasting legendary message: " + broadcastMessage);

            // Broadcast to all online players
            plugin.getServer().broadcastMessage(MessageUtils.colorize(broadcastMessage));

            // Play special sound to all players
            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                try {
                    onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);

                    // Spawn special particles around the lucky player if they're in the same world
                    if (onlinePlayer.getWorld().equals(player.getWorld())) {
                        onlinePlayer.getWorld().spawnParticle(Particle.FIREWORK,
                                player.getLocation().add(0, 2, 0), 50, 1, 1, 1, 0.3);
                        onlinePlayer.getWorld().spawnParticle(Particle.END_ROD,
                                player.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error playing effects for player " + onlinePlayer.getName() + ": " + e.getMessage());
                }
            }

            // Extra effects for the lucky player
            try {
                player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING,
                        player.getLocation().add(0, 1, 0), 100, 1, 2, 1, 0.2);
            } catch (Exception e) {
                plugin.getLogger().warning("Error spawning totem particles: " + e.getMessage());
            }

        } catch (Exception e) {
            plugin.getLogger().severe("Error in sendLegendaryBroadcast: " + e.getMessage());
            e.printStackTrace();
        }
    }
}