package my.pikrew.MedievalRpg;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.UUID;

public class WandListener implements Listener {

    private final TrapLaser plugin;
    private final HashMap<UUID, Block> firstSelections = new HashMap<>();

    public WandListener(TrapLaser plugin) {
        this.plugin = plugin;
    }

    public static void giveTrapWand(org.bukkit.command.CommandSender sender) {
        if (!(sender instanceof Player player)) return;

        ItemStack wand = new ItemStack(Material.STICK);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "trapsellect");
        wand.setItemMeta(meta);
        player.getInventory().addItem(wand);
        player.sendMessage(ChatColor.GREEN + "You received the trap wand!");
    }

    @EventHandler
    public void onSelect(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.STICK || item.getItemMeta() == null) return;
        if (!"trapsellect".equals(ChatColor.stripColor(item.getItemMeta().getDisplayName()))) return;

        Block clicked = e.getClickedBlock();
        if (clicked == null) return;
        e.setCancelled(true);

        UUID uuid = player.getUniqueId();
        if (!firstSelections.containsKey(uuid)) {
            firstSelections.put(uuid, clicked);
            player.sendMessage(ChatColor.YELLOW + "First block selected.");
        } else {
            Block first = firstSelections.remove(uuid);
            Block second = clicked;
            plugin.getLaserManager().createLaser(first, second);
            player.sendMessage(ChatColor.GREEN + "Laser created between the two blocks!");
        }
    }
}

