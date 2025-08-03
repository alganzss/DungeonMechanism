package my.pikrew.herbalCraft.enums;

import org.bukkit.ChatColor;

public enum HerbalTier {
    COMMON("Common", ChatColor.WHITE, 50.0),
    UNCOMMON("Uncommon", ChatColor.GREEN, 25.0),
    RARE("Rare", ChatColor.BLUE, 15.0),
    EPIC("Epic", ChatColor.DARK_PURPLE, 8.0),
    LEGENDARY("Legendary", ChatColor.GOLD, 2.0);

    private final String displayName;
    private final ChatColor color;
    private final double defaultChance;

    HerbalTier(String displayName, ChatColor color, double defaultChance) {
        this.displayName = displayName;
        this.color = color;
        this.defaultChance = defaultChance;
    }

    public String getDisplayName() {
        return color + displayName;
    }

    public ChatColor getColor() {
        return color;
    }

    public double getDefaultChance() {
        return defaultChance;
    }

    public String getColoredName() {
        return color + displayName + ChatColor.RESET;
    }
}
