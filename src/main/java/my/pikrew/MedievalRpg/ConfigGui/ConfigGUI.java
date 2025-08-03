package my.pikrew.MedievalRpg.ConfigGui;

import my.pikrew.MedievalRpg.DungeonMechanism;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ConfigGUI implements Listener {

    private final DungeonMechanism plugin;
    private final Map<UUID, String> awaitingInput = new HashMap<>();
    private final Map<UUID, String> awaitingInputType = new HashMap<>();
    private final Map<UUID, String> lastMenuType = new HashMap<>(); // Track which menu to return to

    public ConfigGUI(DungeonMechanism plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "⚙️ Dungeon Configuration");

        // Door Room Mechanism Section
        gui.setItem(10, createConfigItem(Material.IRON_DOOR,
                ChatColor.YELLOW + "🚪 Door Room Mechanism",
                Arrays.asList(
                        ChatColor.GRAY + "Konfigurasi mekanisme pintu dungeon",
                        ChatColor.GRAY + "• Region: " + ChatColor.WHITE + plugin.getConfig().getString("region", "dungeon"),
                        ChatColor.GRAY + "• Radius: " + ChatColor.WHITE + plugin.getConfig().getInt("radius", 1),
                        ChatColor.GRAY + "• Trigger Block: " + ChatColor.WHITE + plugin.getConfig().getString("trigger-block", "CHISELED_STONE_BRICKS"),
                        ChatColor.GRAY + "• Restore Delay: " + ChatColor.WHITE + plugin.getConfig().getLong("restore-delay", 6) + "s",
                        "",
                        ChatColor.GREEN + "▶ Klik untuk mengubah"
                )));

        // Heal Area Mechanism Section
        gui.setItem(12, createConfigItem(Material.ENCHANTED_GOLDEN_APPLE,
                ChatColor.GREEN + "❤️ Heal Area Mechanism",
                Arrays.asList(
                        ChatColor.GRAY + "Konfigurasi area penyembuhan",
                        ChatColor.GRAY + "• Heal Block: " + ChatColor.WHITE + plugin.getConfig().getString("heal-block", "LAPIS_BLOCK"),
                        ChatColor.GRAY + "• Heal Amount: " + ChatColor.WHITE + plugin.getConfig().getDouble("heal-amount", 1.0),
                        "",
                        ChatColor.GREEN + "▶ Klik untuk mengubah"
                )));

        // Trap Mechanism Section
        gui.setItem(14, createConfigItem(Material.TNT,
                ChatColor.RED + "⚡ Trap Mechanism",
                Arrays.asList(
                        ChatColor.GRAY + "Konfigurasi jebakan dungeon",
                        ChatColor.GRAY + "• Trap Block: " + ChatColor.WHITE + plugin.getConfig().getString("trap.block", "STONECUTTER"),
                        ChatColor.GRAY + "• Trap Region: " + ChatColor.WHITE + plugin.getConfig().getString("trap.region", "dg1"),
                        ChatColor.GRAY + "• Duration: " + ChatColor.WHITE + plugin.getConfig().getInt("trap.duration", 5) + "s",
                        ChatColor.GRAY + "• Particle: " + ChatColor.WHITE + plugin.getConfig().getString("trap.particle", "SNOWFLAKE"),
                        "",
                        ChatColor.GREEN + "▶ Klik untuk mengubah"
                )));

        // Death Respawn System Section
        gui.setItem(16, createConfigItem(Material.TOTEM_OF_UNDYING,
                ChatColor.AQUA + "💀 Death Respawn System",
                Arrays.asList(
                        ChatColor.GRAY + "Konfigurasi sistem respawn",
                        ChatColor.GRAY + "• Dungeon World: " + ChatColor.WHITE + plugin.getConfig().getString("dungeon_world", "DUNGEON"),
                        ChatColor.GRAY + "• Respawn Duration: " + ChatColor.WHITE + plugin.getConfig().getInt("respawn_duration", 10) + "s",
                        ChatColor.GRAY + "• Spawn X: " + ChatColor.WHITE + plugin.getConfig().getDouble("dungeon_spawn.x", 183),
                        ChatColor.GRAY + "• Spawn Y: " + ChatColor.WHITE + plugin.getConfig().getDouble("dungeon_spawn.y", 4),
                        ChatColor.GRAY + "• Spawn Z: " + ChatColor.WHITE + plugin.getConfig().getDouble("dungeon_spawn.z", 16),
                        "",
                        ChatColor.GREEN + "▶ Klik untuk mengubah"
                )));

        // Action Buttons
        gui.setItem(48, createConfigItem(Material.GREEN_CONCRETE,
                ChatColor.GREEN + "✅ Save & Reload",
                Arrays.asList(
                        ChatColor.GRAY + "Simpan semua perubahan dan",
                        ChatColor.GRAY + "muat ulang konfigurasi plugin",
                        "",
                        ChatColor.GREEN + "▶ Klik untuk simpan"
                )));

        gui.setItem(49, createConfigItem(Material.BOOK,
                ChatColor.YELLOW + "📋 View All Config",
                Arrays.asList(
                        ChatColor.GRAY + "Lihat semua konfigurasi",
                        ChatColor.GRAY + "dalam format text",
                        "",
                        ChatColor.YELLOW + "▶ Klik untuk lihat"
                )));

        gui.setItem(50, createConfigItem(Material.RED_CONCRETE,
                ChatColor.RED + "❌ Close",
                Arrays.asList(
                        ChatColor.GRAY + "Tutup menu konfigurasi",
                        "",
                        ChatColor.RED + "▶ Klik untuk tutup"
                )));

        // Decorative items
        fillBorders(gui);

        player.openInventory(gui);
    }

    public void openDoorRoomMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.YELLOW + "🚪 Door Room Configuration");

        gui.setItem(10, createConfigItem(Material.NAME_TAG,
                ChatColor.AQUA + "Region Name",
                Arrays.asList(
                        ChatColor.GRAY + "Current: " + ChatColor.WHITE + plugin.getConfig().getString("region", "dungeon"),
                        "",
                        ChatColor.GREEN + "▶ Klik untuk mengubah"
                )));

        gui.setItem(11, createConfigItem(Material.COMPASS,
                ChatColor.AQUA + "Radius",
                Arrays.asList(
                        ChatColor.GRAY + "Current: " + ChatColor.WHITE + plugin.getConfig().getInt("radius", 1),
                        "",
                        ChatColor.GREEN + "▶ Klik untuk mengubah"
                )));

        gui.setItem(12, createConfigItem(Material.CHISELED_STONE_BRICKS,
                ChatColor.AQUA + "Trigger Block",
                Arrays.asList(
                        ChatColor.GRAY + "Current: " + ChatColor.WHITE + plugin.getConfig().getString("trigger-block", "CHISELED_STONE_BRICKS"),
                        "",
                        ChatColor.GREEN + "▶ Klik untuk mengubah"
                )));

        gui.setItem(13, createConfigItem(Material.CLOCK,
                ChatColor.AQUA + "Restore Delay",
                Arrays.asList(
                        ChatColor.GRAY + "Current: " + ChatColor.WHITE + plugin.getConfig().getLong("restore-delay", 6) + " seconds",
                        "",
                        ChatColor.GREEN + "▶ Klik untuk mengubah"
                )));

        gui.setItem(22, createConfigItem(Material.ARROW,
                ChatColor.YELLOW + "⬅ Back to Main Menu",
                Arrays.asList(
                        ChatColor.GRAY + "Kembali ke menu utama",
                        "",
                        ChatColor.YELLOW + "▶ Klik untuk kembali"
                )));

        fillBorders(gui);
        player.openInventory(gui);
    }

    public void openHealAreaMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.GREEN + "❤️ Heal Area Configuration");

        gui.setItem(11, createConfigItem(Material.LAPIS_BLOCK,
                ChatColor.AQUA + "Heal Block",
                Arrays.asList(
                        ChatColor.GRAY + "Current: " + ChatColor.WHITE + plugin.getConfig().getString("heal-block", "LAPIS_BLOCK"),
                        "",
                        ChatColor.GREEN + "▶ Klik untuk mengubah"
                )));

        gui.setItem(13, createConfigItem(Material.GOLDEN_APPLE,
                ChatColor.AQUA + "Heal Amount",
                Arrays.asList(
                        ChatColor.GRAY + "Current: " + ChatColor.WHITE + plugin.getConfig().getDouble("heal-amount", 1.0) + " hearts",
                        "",
                        ChatColor.GREEN + "▶ Klik untuk mengubah"
                )));

        gui.setItem(22, createConfigItem(Material.ARROW,
                ChatColor.YELLOW + "⬅ Back to Main Menu",
                Arrays.asList(
                        ChatColor.GRAY + "Kembali ke menu utama",
                        "",
                        ChatColor.YELLOW + "▶ Klik untuk kembali"
                )));

        fillBorders(gui);
        player.openInventory(gui);
    }

    public void openTrapMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.RED + "⚡ Trap Configuration");

        gui.setItem(9, createConfigItem(Material.STONECUTTER,
                ChatColor.AQUA + "Trap Block",
                Arrays.asList(
                        ChatColor.GRAY + "Current: " + ChatColor.WHITE + plugin.getConfig().getString("trap.block", "STONECUTTER"),
                        "",
                        ChatColor.GREEN + "▶ Klik untuk mengubah"
                )));

        gui.setItem(11, createConfigItem(Material.NAME_TAG,
                ChatColor.AQUA + "Trap Region",
                Arrays.asList(
                        ChatColor.GRAY + "Current: " + ChatColor.WHITE + plugin.getConfig().getString("trap.region", "dg1"),
                        "",
                        ChatColor.GREEN + "▶ Klik untuk mengubah"
                )));

        gui.setItem(13, createConfigItem(Material.CLOCK,
                ChatColor.AQUA + "Trap Duration",
                Arrays.asList(
                        ChatColor.GRAY + "Current: " + ChatColor.WHITE + plugin.getConfig().getInt("trap.duration", 5) + " seconds",
                        "",
                        ChatColor.GREEN + "▶ Klik untuk mengubah"
                )));

        gui.setItem(15, createConfigItem(Material.FIREWORK_STAR,
                ChatColor.AQUA + "Trap Particle",
                Arrays.asList(
                        ChatColor.GRAY + "Current: " + ChatColor.WHITE + plugin.getConfig().getString("trap.particle", "SNOWFLAKE"),
                        "",
                        ChatColor.GREEN + "▶ Klik untuk mengubah"
                )));

        gui.setItem(22, createConfigItem(Material.ARROW,
                ChatColor.YELLOW + "⬅ Back to Main Menu",
                Arrays.asList(
                        ChatColor.GRAY + "Kembali ke menu utama",
                        "",
                        ChatColor.YELLOW + "▶ Klik untuk kembali"
                )));

        fillBorders(gui);
        player.openInventory(gui);
    }

    public void openRespawnMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 36, ChatColor.AQUA + "💀 Death Respawn Configuration");

        gui.setItem(10, createConfigItem(Material.GRASS_BLOCK,
                ChatColor.AQUA + "Dungeon World",
                Arrays.asList(
                        ChatColor.GRAY + "Current: " + ChatColor.WHITE + plugin.getConfig().getString("dungeon_world", "DUNGEON"),
                        "",
                        ChatColor.GREEN + "▶ Klik untuk mengubahnya"
                )));

        gui.setItem(12, createConfigItem(Material.CLOCK,
                ChatColor.AQUA + "Respawn Duration",
                Arrays.asList(
                        ChatColor.GRAY + "Current: " + ChatColor.WHITE + plugin.getConfig().getInt("respawn_duration", 10) + " seconds",
                        "",
                        ChatColor.GREEN + "▶ Klik untuk mengubah"
                )));

        gui.setItem(14, createConfigItem(Material.ENDER_PEARL,
                ChatColor.AQUA + "Spawn X Coordinate",
                Arrays.asList(
                        ChatColor.GRAY + "Current: " + ChatColor.WHITE + plugin.getConfig().getDouble("dungeon_spawn.x", 183),
                        "",
                        ChatColor.GREEN + "▶ Klik untuk mengubah"
                )));

        gui.setItem(16, createConfigItem(Material.ENDER_PEARL,
                ChatColor.AQUA + "Spawn Y Coordinate",
                Arrays.asList(
                        ChatColor.GRAY + "Current: " + ChatColor.WHITE + plugin.getConfig().getDouble("dungeon_spawn.y", 4),
                        "",
                        ChatColor.GREEN + "▶ Klik untuk mengubah"
                )));

        gui.setItem(20, createConfigItem(Material.ENDER_PEARL,
                ChatColor.AQUA + "Spawn Z Coordinate",
                Arrays.asList(
                        ChatColor.GRAY + "Current: " + ChatColor.WHITE + plugin.getConfig().getDouble("dungeon_spawn.z", 16),
                        "",
                        ChatColor.GREEN + "▶ Klik untuk mengubah"
                )));

        gui.setItem(22, createConfigItem(Material.RECOVERY_COMPASS,
                ChatColor.YELLOW + "📍 Set Current Location",
                Arrays.asList(
                        ChatColor.GRAY + "Set spawn point ke lokasi",
                        ChatColor.GRAY + "kamu saat ini",
                        "",
                        ChatColor.YELLOW + "▶ Klik untuk set"
                )));

        gui.setItem(31, createConfigItem(Material.ARROW,
                ChatColor.YELLOW + "⬅ Back to Main Menu",
                Arrays.asList(
                        ChatColor.GRAY + "Kembali ke menu utama",
                        "",
                        ChatColor.YELLOW + "▶ Klik untuk kembali"
                )));

        fillBorders(gui);
        player.openInventory(gui);
    }

    private ItemStack createConfigItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void fillBorders(Inventory gui) {
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = border.getItemMeta();
        meta.setDisplayName(" ");
        border.setItemMeta(meta);

        int size = gui.getSize();

        // Top and bottom borders
        for (int i = 0; i < 9; i++) {
            if (gui.getItem(i) == null) gui.setItem(i, border);
            if (gui.getItem(size - 9 + i) == null) gui.setItem(size - 9 + i, border);
        }

        // Left and right borders
        for (int i = 0; i < size; i += 9) {
            if (gui.getItem(i) == null) gui.setItem(i, border);
            if (i + 8 < size && gui.getItem(i + 8) == null) gui.setItem(i + 8, border);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String title = event.getView().getTitle();

        if (!title.contains("Configuration") && !title.contains("Dungeon Configuration")) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        ItemStack item = event.getCurrentItem();
        String displayName = item.getItemMeta().getDisplayName();

        if (title.equals(ChatColor.DARK_PURPLE + "⚙️ Dungeon Configuration")) {
            handleMainMenuClick(player, displayName);
        } else if (title.equals(ChatColor.YELLOW + "🚪 Door Room Configuration")) {
            handleDoorRoomClick(player, displayName);
        } else if (title.equals(ChatColor.GREEN + "❤️ Heal Area Configuration")) {
            handleHealAreaClick(player, displayName);
        } else if (title.equals(ChatColor.RED + "⚡ Trap Configuration")) {
            handleTrapClick(player, displayName);
        } else if (title.equals(ChatColor.AQUA + "💀 Death Respawn Configuration")) {
            handleRespawnClick(player, displayName);
        }
    }

    private void handleMainMenuClick(Player player, String displayName) {
        if (displayName.contains("Door Room Mechanism")) {
            openDoorRoomMenu(player);
        } else if (displayName.contains("Heal Area Mechanism")) {
            openHealAreaMenu(player);
        } else if (displayName.contains("Trap Mechanism")) {
            openTrapMenu(player);
        } else if (displayName.contains("Death Respawn System")) {
            openRespawnMenu(player);
        } else if (displayName.contains("Save & Reload")) {
            saveAndReload(player);
        } else if (displayName.contains("View All Config")) {
            showAllConfig(player);
        } else if (displayName.contains("Close")) {
            player.closeInventory();
        }
    }

    private void handleDoorRoomClick(Player player, String displayName) {
        if (displayName.contains("Region Name")) {
            requestInput(player, "region", "string", "Masukkan nama region:", "door");
        } else if (displayName.contains("Radius")) {
            requestInput(player, "radius", "int", "Masukkan radius (angka):", "door");
        } else if (displayName.contains("Trigger Block")) {
            requestInput(player, "trigger-block", "material", "Masukkan nama material block:", "door");
        } else if (displayName.contains("Restore Delay")) {
            requestInput(player, "restore-delay", "long", "Masukkan delay dalam detik:", "door");
        } else if (displayName.contains("Back to Main Menu")) {
            openMainMenu(player);
        }
    }

    private void handleHealAreaClick(Player player, String displayName) {
        if (displayName.contains("Heal Block")) {
            requestInput(player, "heal-block", "material", "Masukkan nama material block:", "heal");
        } else if (displayName.contains("Heal Amount")) {
            requestInput(player, "heal-amount", "double", "Masukkan jumlah heal (decimal):", "heal");
        } else if (displayName.contains("Back to Main Menu")) {
            openMainMenu(player);
        }
    }

    private void handleTrapClick(Player player, String displayName) {
        if (displayName.contains("Trap Block")) {
            requestInput(player, "trap.block", "material", "Masukkan nama material block:", "trap");
        } else if (displayName.contains("Trap Region")) {
            requestInput(player, "trap.region", "string", "Masukkan nama region:", "trap");
        } else if (displayName.contains("Trap Duration")) {
            requestInput(player, "trap.duration", "int", "Masukkan durasi dalam detik:", "trap");
        } else if (displayName.contains("Trap Particle")) {
            requestInput(player, "trap.particle", "string", "Masukkan nama particle:", "trap");
        } else if (displayName.contains("Back to Main Menu")) {
            openMainMenu(player);
        }
    }

    private void handleRespawnClick(Player player, String displayName) {
        if (displayName.contains("Dungeon World")) {
            requestInput(player, "dungeon_world", "string", "Masukkan nama world:", "respawn");
        } else if (displayName.contains("Respawn Duration")) {
            requestInput(player, "respawn_duration", "int", "Masukkan durasi respawn:", "respawn");
        } else if (displayName.contains("Spawn X Coordinate")) {
            requestInput(player, "dungeon_spawn.x", "double", "Masukkan koordinat X:", "respawn");
        } else if (displayName.contains("Spawn Y Coordinate")) {
            requestInput(player, "dungeon_spawn.y", "double", "Masukkan koordinat Y:", "respawn");
        } else if (displayName.contains("Spawn Z Coordinate")) {
            requestInput(player, "dungeon_spawn.z", "double", "Masukkan koordinat Z:", "respawn");
        } else if (displayName.contains("Set Current Location")) {
            setCurrentLocation(player);
        } else if (displayName.contains("Back to Main Menu")) {
            openMainMenu(player);
        }
    }

    private void requestInput(Player player, String configKey, String type, String message, String menuType) {
        awaitingInput.put(player.getUniqueId(), configKey);
        awaitingInputType.put(player.getUniqueId(), type);
        lastMenuType.put(player.getUniqueId(), menuType); // Save which menu to return to
        player.closeInventory();
        player.sendMessage(ChatColor.YELLOW + "📝 " + message);
        player.sendMessage(ChatColor.GRAY + "Ketik " + ChatColor.RED + "cancel" + ChatColor.GRAY + " untuk membatalkan");
    }

    private void setCurrentLocation(Player player) {
        plugin.getConfig().set("dungeon_spawn.x", player.getLocation().getX());
        plugin.getConfig().set("dungeon_spawn.y", player.getLocation().getY());
        plugin.getConfig().set("dungeon_spawn.z", player.getLocation().getZ());

        // Auto save config immediately
        plugin.saveConfig();

        player.sendMessage(ChatColor.GREEN + "✅ Spawn point berhasil diset ke lokasi saat ini!");
        player.sendMessage(ChatColor.GRAY + "📍 X: " + player.getLocation().getX() +
                ", Y: " + player.getLocation().getY() +
                ", Z: " + player.getLocation().getZ());

        // Add delay before reopening menu to avoid conflicts
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            openRespawnMenu(player);
        }, 5L); // 5 ticks delay
    }

    private void saveAndReload(Player player) {
        player.closeInventory();
        player.sendMessage(ChatColor.YELLOW + "⟳ Menyimpan dan memuat ulang konfigurasi...");

        try {
            plugin.saveConfig();
            plugin.reloadConfig();
            plugin.loadSettings();
            plugin.loadRegionSettings();

            player.sendMessage(ChatColor.GREEN + "✅ Konfigurasi berhasil disimpan dan dimuat ulang!");

        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "❌ Error: " + e.getMessage());
            plugin.getLogger().severe("Error saving/reloading config: " + e.getMessage());
        }
    }

    private void showAllConfig(Player player) {
        player.closeInventory();
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.GOLD + "📋 CURRENT DUNGEON CONFIGURATION");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");

        // Door Room
        player.sendMessage(ChatColor.YELLOW + "🚪 Door Room:");
        player.sendMessage(ChatColor.GRAY + "  • region = " + ChatColor.WHITE + plugin.getConfig().getString("region"));
        player.sendMessage(ChatColor.GRAY + "  • radius = " + ChatColor.WHITE + plugin.getConfig().getInt("radius"));
        player.sendMessage(ChatColor.GRAY + "  • trigger-block = " + ChatColor.WHITE + plugin.getConfig().getString("trigger-block"));
        player.sendMessage(ChatColor.GRAY + "  • restore-delay = " + ChatColor.WHITE + plugin.getConfig().getLong("restore-delay"));

        // Heal Area
        player.sendMessage(ChatColor.GREEN + "❤️ Heal Area:");
        player.sendMessage(ChatColor.GRAY + "  • heal-block = " + ChatColor.WHITE + plugin.getConfig().getString("heal-block"));
        player.sendMessage(ChatColor.GRAY + "  • heal-amount = " + ChatColor.WHITE + plugin.getConfig().getDouble("heal-amount"));

        // Trap
        player.sendMessage(ChatColor.RED + "⚡ Trap:");
        player.sendMessage(ChatColor.GRAY + "  • trap.block = " + ChatColor.WHITE + plugin.getConfig().getString("trap.block"));
        player.sendMessage(ChatColor.GRAY + "  • trap.region = " + ChatColor.WHITE + plugin.getConfig().getString("trap.region"));
        player.sendMessage(ChatColor.GRAY + "  • trap.duration = " + ChatColor.WHITE + plugin.getConfig().getInt("trap.duration"));
        player.sendMessage(ChatColor.GRAY + "  • trap.particle = " + ChatColor.WHITE + plugin.getConfig().getString("trap.particle"));

        // Respawn
        player.sendMessage(ChatColor.AQUA + "💀 Death Respawn:");
        player.sendMessage(ChatColor.GRAY + "  • dungeon_world = " + ChatColor.WHITE + plugin.getConfig().getString("dungeon_world"));
        player.sendMessage(ChatColor.GRAY + "  • respawn_duration = " + ChatColor.WHITE + plugin.getConfig().getInt("respawn_duration"));
        player.sendMessage(ChatColor.GRAY + "  • spawn.x = " + ChatColor.WHITE + plugin.getConfig().getDouble("dungeon_spawn.x"));
        player.sendMessage(ChatColor.GRAY + "  • spawn.y = " + ChatColor.WHITE + plugin.getConfig().getDouble("dungeon_spawn.y"));
        player.sendMessage(ChatColor.GRAY + "  • spawn.z = " + ChatColor.WHITE + plugin.getConfig().getDouble("dungeon_spawn.z"));

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!awaitingInput.containsKey(uuid)) return;

        event.setCancelled(true);

        String message = event.getMessage().trim();
        String configKey = awaitingInput.get(uuid);
        String type = awaitingInputType.get(uuid);
        String menuType = lastMenuType.get(uuid);

        if (message.equalsIgnoreCase("cancel")) {
            awaitingInput.remove(uuid);
            awaitingInputType.remove(uuid);
            lastMenuType.remove(uuid);
            player.sendMessage(ChatColor.RED + "❌ Input dibatalkan!");

            // Return to appropriate menu after cancellation
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                returnToMenu(player, menuType);
            }, 5L);
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (setConfigValue(configKey, message, type)) {
                // Auto save config immediately after successful change
                plugin.saveConfig();

                player.sendMessage(ChatColor.GREEN + "✅ Konfigurasi berhasil diubah dan disimpan!");
                player.sendMessage(ChatColor.GRAY + "📝 " + configKey + " = " + ChatColor.WHITE + message);
                player.sendMessage(ChatColor.GREEN + "💾 Config otomatis tersimpan!");

                // Add delay before reopening menu to avoid conflicts
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    returnToMenu(player, menuType);
                }, 10L); // 10 ticks delay (0.5 seconds)

            } else {
                player.sendMessage(ChatColor.RED + "❌ Value tidak valid untuk " + configKey);
                player.sendMessage(ChatColor.GRAY + "Silakan coba lagi dengan format yang benar");

                // Return to menu even if input was invalid
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    returnToMenu(player, menuType);
                }, 10L);
            }

            awaitingInput.remove(uuid);
            awaitingInputType.remove(uuid);
            lastMenuType.remove(uuid);
        });
    }

    private void returnToMenu(Player player, String menuType) {
        if (menuType == null) {
            openMainMenu(player);
            return;
        }

        switch (menuType) {
            case "door":
                openDoorRoomMenu(player);
                break;
            case "heal":
                openHealAreaMenu(player);
                break;
            case "trap":
                openTrapMenu(player);
                break;
            case "respawn":
                openRespawnMenu(player);
                break;
            default:
                openMainMenu(player);
                break;
        }
    }

    private boolean setConfigValue(String key, String value, String type) {
        try {
            switch (type) {
                case "string":
                    plugin.getConfig().set(key, value);
                    return true;

                case "int":
                    int intValue = Integer.parseInt(value);
                    if (intValue < 0 && !key.contains("coordinate")) return false;
                    plugin.getConfig().set(key, intValue);
                    return true;

                case "long":
                    long longValue = Long.parseLong(value);
                    if (longValue < 0) return false;
                    plugin.getConfig().set(key, longValue);
                    return true;

                case "double":
                    double doubleValue = Double.parseDouble(value);
                    plugin.getConfig().set(key, doubleValue);
                    return true;

                case "material":
                    Material material = Material.matchMaterial(value.toUpperCase());
                    if (material == null) return false;
                    plugin.getConfig().set(key, value.toUpperCase());
                    return true;

                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }
}