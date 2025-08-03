package my.pikrew.herbalCraft.managers;

import my.pikrew.MedievalRpg.DungeonMechanism;
import my.pikrew.herbalCraft.enums.HerbalTier;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemManager {

    private final DungeonMechanism plugin; // Changed from herbalCraft to HerbalCraft
    private final NamespacedKey herbalKey;
    private final NamespacedKey potionKey;
    private final Map<HerbalTier, ItemStack> herbalItems;
    private final Map<HerbalTier, ItemStack> potionItems;

    public ItemManager(DungeonMechanism plugin) { // Changed parameter type to HerbalCraft
        this.plugin = plugin;
        this.herbalKey = new NamespacedKey(plugin, "herbal_tier");
        this.potionKey = new NamespacedKey(plugin, "potion_tier");
        this.herbalItems = new HashMap<>();
        this.potionItems = new HashMap<>();

        initializeItems();
    }

    private void initializeItems() {
        // Initialize herbal items
        herbalItems.put(HerbalTier.COMMON, createHerbalItem(HerbalTier.COMMON, "Herbal Grass", Material.WHEAT_SEEDS));
        herbalItems.put(HerbalTier.UNCOMMON, createHerbalItem(HerbalTier.UNCOMMON, "Wild Mint", Material.GREEN_DYE));
        herbalItems.put(HerbalTier.RARE, createHerbalItem(HerbalTier.RARE, "Mystic Flower", Material.BLUE_ORCHID));
        herbalItems.put(HerbalTier.EPIC, createHerbalItem(HerbalTier.EPIC, "Crystal Herb", Material.AMETHYST_SHARD));
        herbalItems.put(HerbalTier.LEGENDARY, createHerbalItem(HerbalTier.LEGENDARY, "Divine Essence", Material.NETHER_STAR));

        // Initialize potion items
        potionItems.put(HerbalTier.COMMON, createPotionItem(HerbalTier.COMMON));
        potionItems.put(HerbalTier.UNCOMMON, createPotionItem(HerbalTier.UNCOMMON));
        potionItems.put(HerbalTier.RARE, createPotionItem(HerbalTier.RARE));
        potionItems.put(HerbalTier.EPIC, createPotionItem(HerbalTier.EPIC));
        potionItems.put(HerbalTier.LEGENDARY, createPotionItem(HerbalTier.LEGENDARY));
    }

    private ItemStack createHerbalItem(HerbalTier tier, String name, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) { // Added null check
            meta.setDisplayName(tier.getColor() + name);
            meta.setLore(Arrays.asList(
                    "§7Tier: " + tier.getColoredName(),
                    "§7A magical herb found in dungeons",
                    "§7Can be used to craft powerful potions"
            ));

            meta.getPersistentDataContainer().set(herbalKey, PersistentDataType.STRING, tier.name());
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createPotionItem(HerbalTier tier) {
        ItemStack item = new ItemStack(Material.POTION);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) { // Added null check
            String potionName = tier.getColor() + getPotionName(tier) + " Potion";
            meta.setDisplayName(potionName);

            List<String> lore = Arrays.asList(
                    "§7Tier: " + tier.getColoredName(),
                    "§7Effects:",
                    getEffectDescription(tier),
                    "§7Duration: " + getDuration(tier) + " seconds",
                    "§eRight-click to consume"
            );
            meta.setLore(lore);

            meta.getPersistentDataContainer().set(potionKey, PersistentDataType.STRING, tier.name());
            item.setItemMeta(meta);
        }

        return item;
    }

    private String getPotionName(HerbalTier tier) {
        switch (tier) {
            case COMMON: return "Healing";
            case UNCOMMON: return "Strength";
            case RARE: return "Speed";
            case EPIC: return "Regeneration";
            case LEGENDARY: return "Divine Power";
            default: return "Unknown";
        }
    }

    private String getEffectDescription(HerbalTier tier) {
        switch (tier) {
            case COMMON: return "§aInstant Health II";
            case UNCOMMON: return "§aStrength II";
            case RARE: return "§aSpeed III + Jump Boost II";
            case EPIC: return "§aRegeneration III + Resistance II";
            case LEGENDARY: return "§aAll Effects + Absorption V";
            default: return "§cUnknown Effect";
        }
    }

    private int getDuration(HerbalTier tier) {
        switch (tier) {
            case COMMON: return 0; // Instant
            case UNCOMMON: return 300; // 5 minutes
            case RARE: return 600; // 10 minutes
            case EPIC: return 900; // 15 minutes
            case LEGENDARY: return 1200; // 20 minutes
            default: return 60;
        }
    }

    public ItemStack getHerbalItem(HerbalTier tier) {
        ItemStack item = herbalItems.get(tier);
        return item != null ? item.clone() : null;
    }

    public ItemStack getPotionItem(HerbalTier tier) {
        ItemStack item = potionItems.get(tier);
        return item != null ? item.clone() : null;
    }

    public HerbalTier getHerbalTier(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        String tierString = meta.getPersistentDataContainer().get(herbalKey, PersistentDataType.STRING);
        if (tierString == null) return null;

        try {
            return HerbalTier.valueOf(tierString);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public HerbalTier getPotionTier(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        String tierString = meta.getPersistentDataContainer().get(potionKey, PersistentDataType.STRING);
        if (tierString == null) return null;

        try {
            return HerbalTier.valueOf(tierString);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public PotionEffect[] getPotionEffects(HerbalTier tier) {
        int duration = getDuration(tier) * 20; // Convert to ticks

        switch (tier) {
            case COMMON:
                return new PotionEffect[] {
                        new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 1)
                };
            case UNCOMMON:
                return new PotionEffect[] {
                        new PotionEffect(PotionEffectType.STRENGTH, duration, 1)
                };
            case RARE:
                return new PotionEffect[] {
                        new PotionEffect(PotionEffectType.SPEED, duration, 2),
                        new PotionEffect(PotionEffectType.JUMP_BOOST, duration, 1)
                };
            case EPIC:
                return new PotionEffect[] {
                        new PotionEffect(PotionEffectType.REGENERATION, duration, 2),
                        new PotionEffect(PotionEffectType.RESISTANCE, duration, 1)
                };
            case LEGENDARY:
                return new PotionEffect[] {
                        new PotionEffect(PotionEffectType.REGENERATION, duration, 3),
                        new PotionEffect(PotionEffectType.STRENGTH, duration, 2),
                        new PotionEffect(PotionEffectType.SPEED, duration, 2),
                        new PotionEffect(PotionEffectType.RESISTANCE, duration, 2),
                        new PotionEffect(PotionEffectType.ABSORPTION, duration, 4)
                };
            default:
                return new PotionEffect[0];
        }
    }
}
