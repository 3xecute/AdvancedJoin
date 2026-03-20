package dev.execute.advancedjoin.gui;

import dev.execute.advancedjoin.AdvancedJoin;
import dev.execute.advancedjoin.manager.ConfigManager;
import dev.execute.advancedjoin.model.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ParticlesMenu {

    private static final int SLOT_BACK = 49;
    private static final int[] INNER_SLOTS = {
            10,11,12,13,14,15,16,
            19,20,21,22,23,24,25,
            28,29,30,31,32,33,34,
            37,38,39,40,41,42,43
    };

    private static Material iconFor(String id) {
        return switch (id.toLowerCase()) {
            case "heart"          -> Material.RED_DYE;
            case "flame"          -> Material.BLAZE_POWDER;
            case "portal"         -> Material.ENDER_PEARL;
            case "happy_villager" -> Material.EMERALD;
            case "end_rod"        -> Material.END_ROD;
            case "none"           -> Material.BARRIER;
            default               -> Material.DRAGON_BREATH;   // 1.21 glowing bottle
        };
    }

    public static Inventory build(AdvancedJoin plugin, Player player) {
        List<ConfigManager.ParticleData> particles = plugin.getConfigManager().getParticles();
        Inventory inv = GuiBuilder.create(54,
                plugin.getLangManager().getString("menu.particles-title"));
        GuiBuilder.fillBorder(inv);

        PlayerData data = plugin.getDatabaseManager().getCached(player.getUniqueId());
        String currentId = data != null ? data.getParticleId() : "heart";

        for (int i = 0; i < particles.size() && i < INNER_SLOTS.length; i++) {
            ConfigManager.ParticleData particle = particles.get(i);
            boolean selected = particle.id().equals(currentId);

            ItemStack item = GuiBuilder.buildItem(iconFor(particle.id()),
                    "&#00ff88&l" + particle.description(),
                    "&7Type: &f" + particle.type(),
                    "&7Count: &f" + particle.count(),
                    "",
                    selected ? "&#00ff88✔ Selected" : "&7▶ Click to select");

            if (selected) item = GuiBuilder.addGlow(item);
            inv.setItem(INNER_SLOTS[i], item);
        }

        inv.setItem(SLOT_BACK, GuiBuilder.navItem(Material.SPECTRAL_ARROW,
                "&c&l← Back", "&7Return to main menu."));

        return inv;
    }

    public static boolean isBackSlot(int slot) { return slot == SLOT_BACK; }

    public static String particleIdAt(int slot, AdvancedJoin plugin) {
        List<ConfigManager.ParticleData> particles = plugin.getConfigManager().getParticles();
        for (int i = 0; i < INNER_SLOTS.length && i < particles.size(); i++) {
            if (INNER_SLOTS[i] == slot) return particles.get(i).id();
        }
        return null;
    }
}
