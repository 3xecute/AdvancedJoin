package dev.execute.advancedjoin.gui;

import dev.execute.advancedjoin.AdvancedJoin;
import dev.execute.advancedjoin.manager.ConfigManager;
import dev.execute.advancedjoin.model.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CategoryMenu {

    private static final int SLOT_BACK = 49;
    private static final int[] INNER_SLOTS = {
            10,11,12,13,14,15,16,
            19,20,21,22,23,24,25,
            28,29,30,31,32,33,34,
            37,38,39,40,41,42,43
    };

    public static Inventory build(AdvancedJoin plugin, Player player, String categoryName) {
        List<ConfigManager.MessageData> messages = categoryName.equalsIgnoreCase("VIP")
                ? plugin.getVipManager().getVipMessages()
                : (plugin.getConfigManager().getCategories().containsKey(categoryName)
                        ? plugin.getConfigManager().getCategories().get(categoryName).messages()
                        : List.of());

        String title = plugin.getLangManager().getString("menu.category-title",
                "%category%", categoryName);
        Inventory inv = GuiBuilder.create(54, title);
        GuiBuilder.fillBorder(inv);

        PlayerData data = plugin.getDatabaseManager().getCached(player.getUniqueId());
        String currentId = data != null ? data.getMessageId() : "default";

        for (int i = 0; i < messages.size() && i < INNER_SLOTS.length; i++) {
            ConfigManager.MessageData msg = messages.get(i);
            boolean selected = msg.id().equals(currentId);
            boolean noAccess = msg.vip() && !player.hasPermission("advancedjoin.vip");

            Material mat = noAccess  ? Material.BARRIER
                         : selected  ? Material.LIME_DYE
                                     : Material.PAPER;

            String statusLine = noAccess ? "&c✖ VIP Required"
                              : selected ? "&#00ff88✔ Selected"
                                         : "&7▶ Click to select";

            ItemStack item = GuiBuilder.buildItem(mat,
                    (msg.vip() ? "&#FFD700&l⭐ " : "&#00ff88") + msg.id(),
                    "&8▸ Preview:",
                    "&f  " + msg.text().replace("%player%", player.getName()),
                    "",
                    statusLine);

            if (selected) item = GuiBuilder.addGlow(item);
            inv.setItem(INNER_SLOTS[i], item);
        }

        inv.setItem(SLOT_BACK, GuiBuilder.navItem(Material.SPECTRAL_ARROW,
                "&c&l← Back", "&7Return to categories."));

        return inv;
    }

    public static boolean isBackSlot(int slot) { return slot == SLOT_BACK; }

    public static String messageIdAt(int slot, String categoryName, AdvancedJoin plugin) {
        List<ConfigManager.MessageData> messages = categoryName.equalsIgnoreCase("VIP")
                ? plugin.getVipManager().getVipMessages()
                : (plugin.getConfigManager().getCategories().containsKey(categoryName)
                        ? plugin.getConfigManager().getCategories().get(categoryName).messages()
                        : List.of());
        for (int i = 0; i < INNER_SLOTS.length && i < messages.size(); i++) {
            if (INNER_SLOTS[i] == slot) return messages.get(i).id();
        }
        return null;
    }
}
