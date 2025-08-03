package my.pikrew.herbalCraft.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import my.pikrew.MedievalRpg.DungeonMechanism;
import my.pikrew.herbalCraft.enums.HerbalTier;
import my.pikrew.herbalCraft.utils.MessageUtils;

public class HerbalCommand implements CommandExecutor {

    private final DungeonMechanism plugin;

    public HerbalCommand(DungeonMechanism plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("herbalcraft.admin")) {
            MessageUtils.sendMessage(sender, "&cYou don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give":
                handleGiveCommand(sender, args);
                break;
            case "reload":
                handleReloadCommand(sender);
                break;
            case "info":
                handleInfoCommand(sender);
                break;
            case "blocks":
                handleBlocksCommand(sender);
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void handleGiveCommand(CommandSender sender, String[] args) {
        if (args.length < 4) {
            MessageUtils.sendMessage(sender, "&cUsage: /herbal give <player> <type> <tier> [amount]");
            MessageUtils.sendMessage(sender, "&cType: herbal, potion");
            MessageUtils.sendMessage(sender, "&cTier: common, uncommon, rare, epic, legendary");
            return;
        }

        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            MessageUtils.sendMessage(sender, "&cPlayer not found!");
            return;
        }

        String type = args[2].toLowerCase();
        String tierString = args[3].toLowerCase();
        int amount = 1;

        if (args.length > 4) {
            try {
                amount = Integer.parseInt(args[4]);
            } catch (NumberFormatException e) {
                MessageUtils.sendMessage(sender, "&cInvalid amount! Must be a number.");
                return;
            }
        }

        HerbalTier tier;
        try {
            tier = HerbalTier.valueOf(tierString.toUpperCase());
        } catch (IllegalArgumentException e) {
            MessageUtils.sendMessage(sender, "&cInvalid tier! Use: common, uncommon, rare, epic, legendary");
            return;
        }

        ItemStack item;
        if (type.equals("herbal")) {
            item = plugin.getItemManager().getHerbalItem(tier);
        } else if (type.equals("potion")) {
            item = plugin.getItemManager().getPotionItem(tier);
        } else {
            MessageUtils.sendMessage(sender, "&cInvalid type! Use: herbal, potion");
            return;
        }

        item.setAmount(amount);
        target.getInventory().addItem(item);

        MessageUtils.sendMessage(sender, "&aGave " + amount + "x " + tier.getDisplayName() + " " + type + " to " + target.getName());
        MessageUtils.sendMessage(target, "&aYou received " + amount + "x " + tier.getDisplayName() + " " + type + "!");
    }

    private void handleReloadCommand(CommandSender sender) {
        plugin.reloadConfig();
        plugin.loadSettings(); // Reload DungeonMechanism settings too
        MessageUtils.sendMessage(sender, "&aConfiguration reloaded!");
    }

    private void handleInfoCommand(CommandSender sender) {
        MessageUtils.sendMessage(sender, "&6=== HerbalCraft Info ===");
        MessageUtils.sendMessage(sender, "&eVersion: " + plugin.getDescription().getVersion());
        MessageUtils.sendMessage(sender, "&eAuthor: " + plugin.getDescription().getAuthors().get(0));
        MessageUtils.sendMessage(sender, "&eDungeon Worlds: " + plugin.getConfig().getStringList("dungeon-worlds"));
        MessageUtils.sendMessage(sender, "&eDrop Chance: " + plugin.getConfig().getDouble("general.drop-chance", 0.1) * 100 + "%");
        MessageUtils.sendMessage(sender, "&eBlock Regen Time: " + plugin.getConfig().getInt("general.block-regeneration-time", 30) + " seconds");
        MessageUtils.sendMessage(sender, "&eRegion Name: " + plugin.getRegionName());
        MessageUtils.sendMessage(sender, "&eTrigger Block: " + plugin.getTriggerBlock().name());
    }

    private void handleBlocksCommand(CommandSender sender) {
        int regeneratingBlocks = 0; // You could track this in BlockManager if needed
        MessageUtils.sendMessage(sender, "&6=== Block Regeneration Status ===");
        MessageUtils.sendMessage(sender, "&eRegeneration enabled: " + plugin.getConfig().getBoolean("block-regeneration.enable-block-breaking", true));
        MessageUtils.sendMessage(sender, "&eRegeneration time: " + plugin.getConfig().getInt("general.block-regeneration-time", 30) + " seconds");
        MessageUtils.sendMessage(sender, "&eCurrently regenerating: " + regeneratingBlocks + " blocks");
        MessageUtils.sendMessage(sender, "&eDungeon restore delay: " + plugin.getDelay() + " seconds");
        MessageUtils.sendMessage(sender, "&eDungeon radius: " + plugin.getRadius() + " blocks");
    }

    private void sendHelp(CommandSender sender) {
        MessageUtils.sendMessage(sender, "&6=== HerbalCraft Commands ===");
        MessageUtils.sendMessage(sender, "&e/herbal give <player> <type> <tier> [amount] &7- Give herbal items");
        MessageUtils.sendMessage(sender, "&e/herbal reload &7- Reload configuration");
        MessageUtils.sendMessage(sender, "&e/herbal info &7- Show plugin information");
        MessageUtils.sendMessage(sender, "&e/herbal blocks &7- Show block regeneration status");
        MessageUtils.sendMessage(sender, "&7");
        MessageUtils.sendMessage(sender, "&7Types: herbal, potion");
        MessageUtils.sendMessage(sender, "&7Tiers: common, uncommon, rare, epic, legendary");
    }
}