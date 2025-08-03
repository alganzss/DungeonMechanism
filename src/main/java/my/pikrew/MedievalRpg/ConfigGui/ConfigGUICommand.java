package my.pikrew.MedievalRpg.ConfigGui;

import my.pikrew.MedievalRpg.DungeonMechanism;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigGUICommand implements CommandExecutor, TabCompleter {

    private final DungeonMechanism plugin;
    private final ConfigGUI configGUI;

    public ConfigGUICommand(DungeonMechanism plugin, ConfigGUI configGUI) {
        this.plugin = plugin;
        this.configGUI = configGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "❌ Command ini hanya bisa digunakan oleh player!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("dungeonmechanism.admin")) {
            player.sendMessage(ChatColor.RED + "❌ Kamu tidak memiliki izin untuk menggunakan command ini!");
            return true;
        }

        if (args.length == 0) {
            configGUI.openMainMenu(player);
            player.sendMessage(ChatColor.GREEN + "✅ GUI Konfigurasi telah dibuka!");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "main":
            case "menu":
                configGUI.openMainMenu(player);
                player.sendMessage(ChatColor.GREEN + "✅ Menu utama dibuka!");
                break;

            case "door":
            case "doorroom":
                configGUI.openDoorRoomMenu(player);
                player.sendMessage(ChatColor.YELLOW + "🚪 Menu Door Room dibuka!");
                break;

            case "heal":
            case "healarea":
                configGUI.openHealAreaMenu(player);
                player.sendMessage(ChatColor.GREEN + "❤️ Menu Heal Area dibuka!");
                break;

            case "trap":
                configGUI.openTrapMenu(player);
                player.sendMessage(ChatColor.RED + "⚡ Menu Trap dibuka!");
                break;

            case "respawn":
            case "death":
                configGUI.openRespawnMenu(player);
                player.sendMessage(ChatColor.AQUA + "💀 Menu Death Respawn dibuka!");
                break;

            case "reload":
                if (!player.hasPermission("dungeon.reload")) {
                    player.sendMessage(ChatColor.RED + "❌ Kamu tidak memiliki izin reload!");
                    return true;
                }

                player.sendMessage(ChatColor.YELLOW + "⟳ Memuat ulang konfigurasi...");

                try {
                    plugin.reloadConfig();
                    plugin.loadSettings();
                    plugin.loadRegionSettings();
                    player.sendMessage(ChatColor.GREEN + "✅ Plugin berhasil dimuat ulang!");
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "❌ Error: " + e.getMessage());
                }
                break;

            case "help":
                sendHelpMessage(player);
                break;

            default:
                player.sendMessage(ChatColor.RED + "❌ Subcommand tidak dikenal: " + subCommand);
                sendHelpMessage(player);
                break;
        }

        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.GOLD + "🔧 DUNGEON CONFIG GUI COMMANDS");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "/dconfig" + ChatColor.GRAY + " - Buka menu utama GUI");
        player.sendMessage(ChatColor.YELLOW + "/dconfig main" + ChatColor.GRAY + " - Buka menu utama");
        player.sendMessage(ChatColor.YELLOW + "/dconfig door" + ChatColor.GRAY + " - Buka menu Door Room");
        player.sendMessage(ChatColor.YELLOW + "/dconfig heal" + ChatColor.GRAY + " - Buka menu Heal Area");
        player.sendMessage(ChatColor.YELLOW + "/dconfig trap" + ChatColor.GRAY + " - Buka menu Trap");
        player.sendMessage(ChatColor.YELLOW + "/dconfig respawn" + ChatColor.GRAY + " - Buka menu Death Respawn");
        player.sendMessage(ChatColor.YELLOW + "/dconfig reload" + ChatColor.GRAY + " - Reload plugin");
        player.sendMessage(ChatColor.YELLOW + "/dconfig help" + ChatColor.GRAY + " - Tampilkan help ini");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.GRAY + "💡 Aliases: /dungeonconfig, /dcfg");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList(
                    "main", "menu", "door", "doorroom", "heal", "healarea",
                    "trap", "respawn", "death", "reload", "help"
            ));
        }

        return completions;
    }
}