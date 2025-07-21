package my.pikrew.mmoitemsdungeon;

import net.raidstone.wgevents.events.RegionEnteredEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class RegionEntryListener implements Listener {

    private final Logger log;

    public RegionEntryListener(Logger logger) {
        this.log = logger;
    }

    @EventHandler
    public void onRegionEntered(RegionEnteredEvent event) {
        Player player = event.getPlayer();

        if (!event.getRegionName().equalsIgnoreCase("dg1")) return;

        log.info("[DEBUG] Player " + player.getName() + " masuk region dg1. Memeriksa armor...");

        boolean hasRequiredLore = Arrays.stream(player.getInventory().getArmorContents())
                .filter(Objects::nonNull)
                .map(item -> {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null && meta.hasLore()) {
                        List<String> lore = meta.getLore();
                        if (lore != null) {
                            for (String line : lore) {
                                log.info("[DEBUG] Lore: " + ChatColor.stripColor(line));
                                if (ChatColor.stripColor(line).toLowerCase().contains("pikrew")) {
                                    return true;
                                }
                            }
                        }
                    }
                    log.info("[DEBUG] Armor TIDAK memiliki lore yang mengandung 'pikrew'");
                    return false;
                })
                .anyMatch(found -> found);



        if (!hasRequiredLore) {
            // Dorong player ke belakang
            Vector push = player.getLocation().getDirection().multiply(-1.5).setY(0.5);
            player.setVelocity(push);

            // Kirim Title
            player.sendTitle(
                    ChatColor.RED + "Kamu tidak memenuhi persyaratan",
                    ChatColor.GRAY + "Gunakan armor yang sesuai!",
                    10, 60, 10
            );

            log.info("[DEBUG] Player " + player.getName() + " TIDAK memenuhi syarat dan dipental.");
        } else {
            log.info("[DEBUG] Player " + player.getName() + " memenuhi syarat, tidak dipental.");
        }
    }
}
