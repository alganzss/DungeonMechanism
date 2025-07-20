package my.pikrew.mmoitemsdungeon;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.raidstone.wgevents.events.RegionEnteredEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Arrays;
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

        boolean hasRequiredTag = Arrays.stream(player.getInventory().getArmorContents())
                .filter(Objects::nonNull)
                .map(NBTItem::get)
                .filter(nbt -> {
                    boolean hasTag = nbt.hasTag("pikrew");
                    if (hasTag) {
                        log.info("[DEBUG] Armor memiliki tag pikrew = " + nbt.getString("pikrew"));
                    } else {
                        log.info("[DEBUG] Armor TIDAK memiliki tag pikrew.");
                    }
                    return hasTag;
                })
                .anyMatch(nbt -> "1".equals(nbt.getString("pikrew")));


        if (!hasRequiredTag) {
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
