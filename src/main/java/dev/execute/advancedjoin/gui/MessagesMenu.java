package dev.execute.advancedjoin.gui;

import dev.execute.advancedjoin.AdvancedJoin;
import dev.execute.advancedjoin.manager.ConfigManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MessagesMenu {

    /*
     * 6 item (5 kategori + VIP):
     * Row 2: slots 20,21,22,23,24  (5 yan yana)
     * Row 4: slot  22              (VIP ortada)
     * Back:  slot  49
     */
    private static final int[] CATEGORY_SLOTS = {20, 21, 22, 23, 24};
    private static final int   SLOT_VIP  = 31;
    private static final int   SLOT_BACK = 49;

    private static Material categoryIcon(String name) {
        return switch (name.toUpperCase()) {
            case "DARK"      -> Material.INK_SAC;
            case "FUN"       -> Material.SLIME_BALL;
            case "SCIENCE"   -> Material.RECOVERY_COMPASS;
            case "SURPRISED" -> Material.WIND_CHARGE;
            case "CURIOUS"   -> Material.SPYGLASS;
            default          -> Material.PAPER;
        };
    }

    public static Inventory build(AdvancedJoin plugin, Player player) {
        Map<String, ConfigManager.CategoryData> categories =
                plugin.getMessageManager().getVisibleCategories(player);

        Inventory inv = GuiBuilder.create(54,
                plugin.getLangManager().getString("menu.messages-title"));
        GuiBuilder.fillBorder(inv);

        List<Map.Entry<String, ConfigManager.CategoryData>> entries = new ArrayList<>(categories.entrySet());

        for (int i = 0; i < entries.size() && i < CATEGORY_SLOTS.length; i++) {
            String name = entries.get(i).getKey();
            ConfigManager.CategoryData data = entries.get(i).getValue();

            long available = data.messages().stream()
                    .filter(m -> !m.vip() || player.hasPermission("advancedjoin.vip"))
                    .count();

            inv.setItem(CATEGORY_SLOTS[i], GuiBuilder.buildItem(
                    categoryIcon(name),
                    "&#00ff88&l" + name,
                    "&f" + available + "&7/" + data.messages().size() + " messages",
                    "",
                    "&a▶ Click to browse"));
        }

        // VIP — orta alt satır
        boolean hasVip = player.hasPermission("advancedjoin.vip");
        int vipCount = plugin.getVipManager().getVipMessages().size();
        ItemStack vipItem = GuiBuilder.buildItem(
                hasVip ? Material.TRIAL_KEY : Material.IRON_INGOT,
                "&#FFD700&l⭐ VIP",
                "&7" + vipCount + " exclusive messages",
                "",
                hasVip ? "&a▶ Click to browse" : "&c✖ VIP permission required");
        if (hasVip) vipItem = GuiBuilder.addGlow(vipItem);
        inv.setItem(SLOT_VIP, vipItem);

        inv.setItem(SLOT_BACK, GuiBuilder.navItem(Material.SPECTRAL_ARROW,
                "&c&l← Back", "&7Return to main menu."));

        return inv;
    }

    public static boolean isBackSlot(int slot) { return slot == SLOT_BACK; }

    public static String categoryAt(Inventory inv, int slot, AdvancedJoin plugin) {
        if (slot == SLOT_VIP) return "VIP";
        Map<String, ConfigManager.CategoryData> categories = plugin.getConfigManager().getCategories();
        List<String> names = new ArrayList<>(categories.keySet());
        for (int i = 0; i < CATEGORY_SLOTS.length && i < names.size(); i++) {
            if (CATEGORY_SLOTS[i] == slot) return names.get(i);
        }
        return null;
    }
}