package my.pikrew.herbalCraft.managers;

import my.pikrew.MedievalRpg.DungeonMechanism;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.RecipeChoice;
import my.pikrew.herbalCraft.enums.HerbalTier;

public class CraftingManager {

    private final DungeonMechanism plugin;

    public CraftingManager(DungeonMechanism plugin) {
        this.plugin = plugin;
        registerRecipes();
    }

    private void registerRecipes() {
        // Register recipes for each tier
        for (HerbalTier tier : HerbalTier.values()) {
            registerPotionRecipe(tier);
        }
    }

    private void registerPotionRecipe(HerbalTier tier) {
        ItemStack result = plugin.getItemManager().getPotionItem(tier);
        ItemStack herbalItem = plugin.getItemManager().getHerbalItem(tier);

        NamespacedKey key = new NamespacedKey(plugin, "potion_" + tier.name().toLowerCase());
        ShapedRecipe recipe = new ShapedRecipe(key, result);

        // Set pattern based on tier
        switch (tier) {
            case COMMON:
                recipe.shape(" H ", " W ", " G ");
                recipe.setIngredient('H', new RecipeChoice.ExactChoice(herbalItem));
                recipe.setIngredient('W', Material.WATER_BUCKET);
                recipe.setIngredient('G', Material.GLASS_BOTTLE);
                break;

            case UNCOMMON:
                recipe.shape(" H ", "HWH", " G ");
                recipe.setIngredient('H', new RecipeChoice.ExactChoice(herbalItem));
                recipe.setIngredient('W', Material.WATER_BUCKET);
                recipe.setIngredient('G', Material.GLASS_BOTTLE);
                break;

            case RARE:
                recipe.shape("HHH", "HWH", " G ");
                recipe.setIngredient('H', new RecipeChoice.ExactChoice(herbalItem));
                recipe.setIngredient('W', Material.WATER_BUCKET);
                recipe.setIngredient('G', Material.GLASS_BOTTLE);
                break;

            case EPIC:
                recipe.shape("HHH", "HWH", "HGH");
                recipe.setIngredient('H', new RecipeChoice.ExactChoice(herbalItem));
                recipe.setIngredient('W', Material.WATER_BUCKET);
                recipe.setIngredient('G', Material.GLASS_BOTTLE);
                break;

            case LEGENDARY:
                recipe.shape("HHH", "HWH", "HGH");
                recipe.setIngredient('H', new RecipeChoice.ExactChoice(herbalItem));
                recipe.setIngredient('W', Material.LAVA_BUCKET); // Special ingredient for legendary
                recipe.setIngredient('G', Material.DRAGON_BREATH); // Special ingredient for legendary
                break;
        }

        // Add recipe to server
        try {
            Bukkit.addRecipe(recipe);
            plugin.getLogger().info("Registered " + tier.getDisplayName() + " potion recipe");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to register " + tier.getDisplayName() + " potion recipe: " + e.getMessage());
        }
    }

    public void removeRecipes() {
        for (HerbalTier tier : HerbalTier.values()) {
            NamespacedKey key = new NamespacedKey(plugin, "potion_" + tier.name().toLowerCase());
            Bukkit.removeRecipe(key);
        }
    }
}