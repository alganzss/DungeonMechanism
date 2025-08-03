package my.pikrew.herbalCraft.listeners;

import my.pikrew.MedievalRpg.DungeonMechanism;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import my.pikrew.herbalCraft.enums.HerbalTier;
import my.pikrew.herbalCraft.utils.MessageUtils;

public class PotionConsumeListener implements Listener {

    private final DungeonMechanism plugin;

    public PotionConsumeListener(DungeonMechanism plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check if it's a right click action
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if item exists and is not null
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        // Debug: Print item information
        if (item.hasItemMeta() && item.getItemMeta() != null) {
            ItemMeta meta = item.getItemMeta();
            player.sendMessage("§7[DEBUG] Item: " + item.getType().name());
            player.sendMessage("§7[DEBUG] Display Name: " + (meta.hasDisplayName() ? meta.getDisplayName() : "None"));
            player.sendMessage("§7[DEBUG] Has PDC: " + !meta.getPersistentDataContainer().isEmpty());
        }

        // Check if this is our custom potion
        HerbalTier tier = plugin.getItemManager().getPotionTier(item);
        if (tier == null) {
            return; // Not our custom potion
        }

        // Debug: Confirm potion tier detected
        player.sendMessage("§a[DEBUG] Potion tier detected: " + tier.name());

        // Apply potion effects
        PotionEffect[] effects = plugin.getItemManager().getPotionEffects(tier);
        if (effects != null && effects.length > 0) {
            for (PotionEffect effect : effects) {
                if (effect != null) {
                    player.addPotionEffect(effect, true); // Force override existing effects
                    player.sendMessage("§a[DEBUG] Applied effect: " + effect.getType().getName());
                }
            }
        } else {
            player.sendMessage("§c[DEBUG] No effects found for tier: " + tier.name());
        }

        // Remove item from inventory
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }

        // Play sound
        try {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);
        } catch (Exception e) {
            // Fallback sound if ENTITY_PLAYER_BURP doesn't exist in this version
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1.0f, 1.0f);
        }

        // Send message
        try {
            String configMessage = plugin.getConfig().getString("messages.potion-consumed");
            if (configMessage != null && !configMessage.isEmpty()) {
                String displayName = "Unknown Potion";
                if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasDisplayName()) {
                    displayName = item.getItemMeta().getDisplayName();
                }

                String message = configMessage
                        .replace("{tier}", tier.getDisplayName())
                        .replace("{potion}", displayName);
                MessageUtils.sendMessage(player, message);
            } else {
                // Fallback message if config is not set
                player.sendMessage("§aYou consumed a " + tier.getDisplayName() + " potion!");
            }
        } catch (Exception e) {
            // Fallback message if there's any error
            player.sendMessage("§aYou consumed a " + tier.getDisplayName() + " potion!");
        }

        // Cancel the event to prevent default behavior
        event.setCancelled(true);
    }
}