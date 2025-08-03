package my.pikrew.MedievalRpg;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeathEventCommand implements CommandExecutor {

    private final DungeonMechanism plugin;

    public DeathEventCommand(DungeonMechanism plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("dungeonspawn")) {

            // Cek permission
            if (!sender.hasPermission("dungeonmechanism.admin")) {
                sender.sendMessage(ChatColor.RED + "❌ Kamu tidak memiliki permission!");
                return true;
            }

            if (args.length == 0) {
                sender.sendMessage(ChatColor.YELLOW + "📍 Dungeon Spawn Commands:");
                sender.sendMessage(ChatColor.WHITE + "/dungeonspawn set - Set spawn point di lokasi kamu");
                sender.sendMessage(ChatColor.WHITE + "/dungeonspawn tp - Teleport ke spawn point");
                sender.sendMessage(ChatColor.WHITE + "/dungeonspawn info - Lihat info spawn point");
                return true;
            }

            if (args[0].equalsIgnoreCase("set")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "❌ Command ini hanya bisa digunakan oleh player!");
                    return true;
                }

                Player player = (Player) sender;
                Location loc = player.getLocation();

                // Set koordinat ke config
                plugin.getConfig().set("dungeon_spawn.x", loc.getX());
                plugin.getConfig().set("dungeon_spawn.y", loc.getY());
                plugin.getConfig().set("dungeon_spawn.z", loc.getZ());
                plugin.getConfig().set("dungeon_world", loc.getWorld().getName());
                plugin.saveConfig();

                sender.sendMessage(ChatColor.GREEN + "✅ Dungeon spawn point telah diset!");
                sender.sendMessage(ChatColor.YELLOW + "📍 Koordinat: " +
                        ChatColor.WHITE + String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ()));
                sender.sendMessage(ChatColor.YELLOW + "🌍 World: " + ChatColor.WHITE + loc.getWorld().getName());

                plugin.getLogger().info("Admin " + player.getName() + " set dungeon spawn to: " +
                        loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + " in " + loc.getWorld().getName());
                return true;
            }

            if (args[0].equalsIgnoreCase("tp")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "❌ Command ini hanya bisa digunakan oleh player!");
                    return true;
                }

                Player player = (Player) sender;

                // Ambil koordinat dari config
                double spawnX = plugin.getConfig().getDouble("dungeon_spawn.x", 183);
                double spawnY = plugin.getConfig().getDouble("dungeon_spawn.y", 4);
                double spawnZ = plugin.getConfig().getDouble("dungeon_spawn.z", 16);
                String worldName = plugin.getConfig().getString("dungeon_world", "DUNGEON");

                org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldName);
                if (world == null) {
                    sender.sendMessage(ChatColor.RED + "❌ World '" + worldName + "' tidak ditemukan!");
                    return true;
                }

                Location spawnLoc = new Location(world, spawnX, spawnY, spawnZ);
                player.teleport(spawnLoc);

                sender.sendMessage(ChatColor.GREEN + "✅ Teleported ke dungeon spawn point!");
                sender.sendMessage(ChatColor.YELLOW + "📍 " + String.format("%.1f, %.1f, %.1f", spawnX, spawnY, spawnZ));
                return true;
            }

            if (args[0].equalsIgnoreCase("info")) {
                double spawnX = plugin.getConfig().getDouble("dungeon_spawn.x", 183);
                double spawnY = plugin.getConfig().getDouble("dungeon_spawn.y", 4);
                double spawnZ = plugin.getConfig().getDouble("dungeon_spawn.z", 16);
                String worldName = plugin.getConfig().getString("dungeon_world", "DUNGEON");

                sender.sendMessage(ChatColor.GOLD + "📍 Dungeon Spawn Point Info:");
                sender.sendMessage(ChatColor.YELLOW + "Koordinat: " + ChatColor.WHITE +
                        String.format("%.1f, %.1f, %.1f", spawnX, spawnY, spawnZ));
                sender.sendMessage(ChatColor.YELLOW + "World: " + ChatColor.WHITE + worldName);
                sender.sendMessage(ChatColor.YELLOW + "Respawn Duration: " + ChatColor.WHITE +
                        plugin.getConfig().getInt("respawn_duration", 10) + " detik");
                return true;
            }

            sender.sendMessage(ChatColor.RED + "❌ Subcommand tidak valid! Gunakan: set, tp, atau info");
            return true;
        }

        return false;
    }
}
