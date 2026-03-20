package dev.execute.advancedjoin.gui;

import dev.execute.advancedjoin.AdvancedJoin;
import dev.execute.advancedjoin.manager.ConfigManager;
import dev.execute.advancedjoin.model.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SoundsMenu {

    private static final int SLOT_BACK = 49;
    private static final int[] INNER_SLOTS = {
            10,11,12,13,14,15,16,
            19,20,21,22,23,24,25,
            28,29,30,31,32,33,34,
            37,38,39,40,41,42,43
    };

    private static Material iconFor(String id) {
        return switch (id.toLowerCase()) {
            case "levelup"  -> Material.EXPERIENCE_BOTTLE;
            case "pling"    -> Material.NOTE_BLOCK;
            case "bell"     -> Material.BELL;
            case "chime"    -> Material.WIND_CHARGE;        // 1.21
            case "enderman" -> Material.ENDER_EYE;
            case "none"     -> Material.BARRIER;
            default         -> Material.JUKEBOX;
        };
    }

    public static Inventory build(AdvancedJoin plugin, Player player) {
        List<ConfigManager.SoundData> sounds = plugin.getConfigManager().getSounds();
        Inventory inv = GuiBuilder.create(54,
                plugin.getLangManager().getString("menu.sounds-title"));
        GuiBuilder.fillBorder(inv);

        PlayerData data = plugin.getDatabaseManager().getCached(player.getUniqueId());
        String currentId = data != null ? data.getSoundId() : "levelup";

        for (int i = 0; i < sounds.size() && i < INNER_SLOTS.length; i++) {
            ConfigManager.SoundData sound = sounds.get(i);
            boolean selected = sound.id().equals(currentId);

            ItemStack item = GuiBuilder.buildItem(iconFor(sound.id()),
                    "&#00ff88&l" + sound.description(),
                    "&7" + sound.sound(),
                    "",
                    selected ? "&#00ff88✔ Selected" : "&7▶ Click to select",
                    "&8Right-click to preview");

            if (selected) item = GuiBuilder.addGlow(item);
            inv.setItem(INNER_SLOTS[i], item);
        }

        inv.setItem(SLOT_BACK, GuiBuilder.navItem(Material.SPECTRAL_ARROW,
                "&c&l← Back", "&7Return to main menu."));

        return inv;
    }

    public static boolean isBackSlot(int slot) { return slot == SLOT_BACK; }

    public static String soundIdAt(int slot, AdvancedJoin plugin) {
        List<ConfigManager.SoundData> sounds = plugin.getConfigManager().getSounds();
        for (int i = 0; i < INNER_SLOTS.length && i < sounds.size(); i++) {
            if (INNER_SLOTS[i] == slot) return sounds.get(i).id();
        }
        return null;
    }
}
