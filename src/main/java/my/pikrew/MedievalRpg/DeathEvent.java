package my.pikrew.MedievalRpg;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeathEvent implements Listener {

    private final DungeonMechanism plugin;
    private Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private Map<UUID, ItemStack[]> savedArmor = new HashMap<>();
    private Map<UUID, Boolean> spectatorPlayers = new HashMap<>();
    private Map<UUID, org.bukkit.Location> frozenLocations = new HashMap<>();

    public DeathEvent(DungeonMechanism plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Cek apakah player sedang dalam mode spectator karena mati di dungeon
        if (spectatorPlayers.getOrDefault(playerId, false) && frozenLocations.containsKey(playerId)) {
            // Batalkan movement dan kembalikan ke posisi frozen
            org.bukkit.Location frozenLoc = frozenLocations.get(playerId);

            // Hanya cancel jika player mencoba bergerak dari posisi frozen
            if (event.getTo().distance(frozenLoc) > 0.1) {
                event.setCancelled(true);
                player.teleport(frozenLoc);

                // Opsional: kirim pesan reminder
                if (System.currentTimeMillis() % 3000 < 100) { // Setiap 3 detik sekali
                    player.sendMessage(ChatColor.RED + "⚠ Kamu tidak bisa bergerak saat menunggu respawn!");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID playerId = player.getUniqueId();

        // Cek apakah player mati di world DUNGEON (dapat dikonfigurasi)
        String worldName = player.getWorld().getName();
        String dungeonWorldName = plugin.getConfig().getString("dungeon_world", "DUNGEON");

        if (!worldName.equalsIgnoreCase(dungeonWorldName)) {
            // Jika tidak di world DUNGEON, gunakan sistem death normal
            plugin.getLogger().info("Player " + player.getName() + " mati di world " + worldName + " (bukan dungeon)");
            return;
        }

        plugin.getLogger().info("Player " + player.getName() + " mati di DUNGEON - aktivating respawn system");

        // Simpan inventory dan armor player sebelum mati
        savedInventories.put(playerId, player.getInventory().getContents().clone());
        savedArmor.put(playerId, player.getInventory().getArmorContents().clone());

        // Mencegah item drop saat mati
        event.setKeepInventory(true);
        event.setKeepLevel(true);
        event.getDrops().clear();

        // Set player ke spectator mode
        spectatorPlayers.put(playerId, true);

        // Clear inventory sementara untuk efek visual
        player.getInventory().clear();

        // Kirim pesan kematian khusus untuk DUNGEON
        player.sendMessage(ChatColor.DARK_RED + "💀 Kamu mati di DUNGEON! 💀");
        player.sendMessage(ChatColor.YELLOW + "Sistem respawn dungeon aktif...");
        player.sendMessage(ChatColor.YELLOW + "Menunggu respawn dalam 10 detik...");
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Hanya aktifkan sistem khusus jika player ada di daftar spectator
        // (yang berarti mereka mati di DUNGEON)
        if (spectatorPlayers.containsKey(playerId) && spectatorPlayers.get(playerId)) {
            plugin.getLogger().info("Setting " + player.getName() + " to spectator mode");

            // Set gamemode ke spectator
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.setGameMode(GameMode.SPECTATOR);
                // Simpan lokasi untuk membekukan player
                frozenLocations.put(playerId, player.getLocation().clone());
                // Mulai countdown 10 detik
                startRespawnCountdown(player);
            }, 1L);
        }
    }

    private void startRespawnCountdown(Player player) {
        UUID playerId = player.getUniqueId();
        int duration = plugin.getConfig().getInt("respawn_duration", 10);

        new BukkitRunnable() {
            int countdown = duration;

            @Override
            public void run() {
                if (!player.isOnline() || !spectatorPlayers.getOrDefault(playerId, false)) {
                    cancel();
                    return;
                }

                if (countdown <= 0) {
                    // Respawn player setelah countdown selesai
                    respawnPlayer(player);
                    cancel();
                    return;
                }

                // Tampilkan countdown khusus untuk DUNGEON
                String message = ChatColor.GOLD + "⏰ Respawn DUNGEON dalam: " + ChatColor.RED + countdown + ChatColor.GOLD + " detik";
                player.sendTitle(
                        ChatColor.DARK_RED + "💀 MATI DI DUNGEON 💀",
                        message,
                        5, 15, 5
                );

                // Kirim pesan ke chat
                player.sendMessage(ChatColor.YELLOW + "Respawn dungeon dalam " + ChatColor.RED + countdown + ChatColor.YELLOW + " detik...");

                // Play sound effect
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);

                countdown--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second (20 ticks)
    }

    private void respawnPlayer(Player player) {
        UUID playerId = player.getUniqueId();

        plugin.getLogger().info("Respawning player " + player.getName() + " from dungeon death");

        // Set gamemode kembali ke survival
        player.setGameMode(GameMode.SURVIVAL);

        // Restore inventory dan armor
        if (savedInventories.containsKey(playerId)) {
            player.getInventory().setContents(savedInventories.get(playerId));
            savedInventories.remove(playerId);
        }

        if (savedArmor.containsKey(playerId)) {
            player.getInventory().setArmorContents(savedArmor.get(playerId));
            savedArmor.remove(playerId);
        }

        // Set health dan food ke maximum
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20f);

        // Remove dari spectator list dan frozen location
        spectatorPlayers.remove(playerId);
        frozenLocations.remove(playerId);

        // Kirim pesan respawn khusus untuk DUNGEON
        player.sendTitle(
                ChatColor.GREEN + "⚡ RESPAWN DUNGEON! ⚡",
                ChatColor.GOLD + "Kamu kembali hidup di dungeon!",
                10, 40, 10
        );

        player.sendMessage(ChatColor.GREEN + "✅ Respawn dungeon berhasil!");
        player.sendMessage(ChatColor.YELLOW + "Semua item dungeon telah dikembalikan!");
        player.sendMessage(ChatColor.GRAY + "Hati-hati, kamu masih di dalam DUNGEON...");

        // Play respawn sound
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

        // Add some visual effects
        player.getWorld().spawnParticle(
                org.bukkit.Particle.CRIT,
                player.getLocation().add(0, 1, 0),
                50,
                1, 1, 1,
                0.1
        );
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();

        // Cleanup data player yang disconnect
        savedInventories.remove(playerId);
        savedArmor.remove(playerId);
        spectatorPlayers.remove(playerId);
        frozenLocations.remove(playerId);
    }
}