package dev.execute.advancedjoin.gui;

import dev.execute.advancedjoin.AdvancedJoin;
import dev.execute.advancedjoin.manager.ConfigManager;
import dev.execute.advancedjoin.model.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class AnimationMenu {

    private static final int SLOT_BACK = 49;
    private static final int[] INNER_SLOTS = {
            10,11,12,13,14,15,16,
            19,20,21,22,23,24,25,
            28,29,30,31,32,33,34,
            37,38,39,40,41,42,43
    };

    private static Material iconFor(String id) {
        return switch (id.toUpperCase()) {
            case "NONE"          -> Material.BARRIER;
            case "TYPEWRITER"    -> Material.FEATHER;
            case "GRADIENT_FLOW" -> Material.NAUTILUS_SHELL;
            case "PULSE"         -> Material.HEART_OF_THE_SEA;
            default              -> Material.CLOCK;
        };
    }

    public static Inventory build(AdvancedJoin plugin, Player player) {
        List<ConfigManager.AnimationData> animations = plugin.getConfigManager().getAnimations();
        Inventory inv = GuiBuilder.create(54, "&#00ff88&lJoin Animations");
        GuiBuilder.fillBorder(inv);

        PlayerData data = plugin.getDatabaseManager().getCached(player.getUniqueId());
        String currentId = data != null ? data.getAnimationId() : "NONE";

        for (int i = 0; i < animations.size() && i < INNER_SLOTS.length; i++) {
            ConfigManager.AnimationData anim = animations.get(i);
            boolean selected = anim.id().equalsIgnoreCase(currentId);

            ItemStack item = GuiBuilder.buildItem(iconFor(anim.id()),
                    "&#00ff88&l" + anim.description(),
                    "&7ID: &f" + anim.id(),
                    "&7Speed: &f" + anim.speed(),
                    "",
                    selected ? "&#00ff88✔ Selected" : "&7▶ Click to select");

            if (selected) item = GuiBuilder.addGlow(item);
            inv.setItem(INNER_SLOTS[i], item);
        }

        inv.setItem(SLOT_BACK, GuiBuilder.navItem(Material.SPECTRAL_ARROW,
                "&c&l← Back", "&7Return to settings."));

        return inv;
    }

    public static boolean isBackSlot(int slot) { return slot == SLOT_BACK; }

    public static String animationIdAt(int slot, AdvancedJoin plugin) {
        List<ConfigManager.AnimationData> animations = plugin.getConfigManager().getAnimations();
        for (int i = 0; i < INNER_SLOTS.length && i < animations.size(); i++) {
            if (INNER_SLOTS[i] == slot) return animations.get(i).id();
        }
        return null;
    }

    // expose for GuiListener
    public static int getSlotBack() { return SLOT_BACK; }
}
