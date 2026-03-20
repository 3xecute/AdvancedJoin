package dev.execute.advancedjoin.gui;

import dev.execute.advancedjoin.AdvancedJoin;
import dev.execute.advancedjoin.manager.ConfigManager;
import dev.execute.advancedjoin.model.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class QuitMessageMenu {

    private static final int SLOT_BACK = 49;
    private static final int[] INNER_SLOTS = {
            10,11,12,13,14,15,16,
            19,20,21,22,23,24,25,
            28,29,30,31,32,33,34,
            37,38,39,40,41,42,43
    };

    public static Inventory build(AdvancedJoin plugin, Player player) {
        List<ConfigManager.MessageData> quitMsgs = plugin.getConfigManager().getQuitMessages();
        Inventory inv = GuiBuilder.create(54, "&#ff4444&lQuit Messages");
        GuiBuilder.fillBorder(inv);

        PlayerData data = plugin.getDatabaseManager().getCached(player.getUniqueId());
        String currentId = data != null ? data.getQuitMessageId() : "default";

        // Default slot
        boolean defaultSelected = currentId.equals("default");
        ItemStack defItem = GuiBuilder.buildItem(
                defaultSelected ? Material.LIME_DYE : Material.PAPER,
                "&#ff4444&lDefault",
                "&8Preview:",
                "&f  " + plugin.getLangManager().getString("quit.default-message")
                        .replace("%player%", player.getName()),
                "",
                defaultSelected ? "&#00ff88✔ Selected" : "&7▶ Click to select");
        if (defaultSelected) defItem = GuiBuilder.addGlow(defItem);
        inv.setItem(INNER_SLOTS[0], defItem);

        for (int i = 0; i < quitMsgs.size() && (i + 1) < INNER_SLOTS.length; i++) {
            ConfigManager.MessageData msg = quitMsgs.get(i);
            boolean selected = msg.id().equals(currentId);

            ItemStack item = GuiBuilder.buildItem(
                    selected ? Material.LIME_DYE : Material.PAPER,
                    "&#ff4444" + msg.id(),
                    "&8Preview:",
                    "&f  " + msg.text().replace("%player%", player.getName()),
                    "",
                    selected ? "&#00ff88✔ Selected" : "&7▶ Click to select");

            if (selected) item = GuiBuilder.addGlow(item);
            inv.setItem(INNER_SLOTS[i + 1], item);
        }

        inv.setItem(SLOT_BACK, GuiBuilder.navItem(Material.SPECTRAL_ARROW,
                "&c&l← Back", "&7Return to main menu."));

        return inv;
    }

    public static boolean isBackSlot(int slot) { return slot == SLOT_BACK; }

    public static String messageIdAt(int slot, AdvancedJoin plugin) {
        List<ConfigManager.MessageData> msgs = plugin.getConfigManager().getQuitMessages();
        if (INNER_SLOTS[0] == slot) return "default";
        for (int i = 0; i < msgs.size() && (i + 1) < INNER_SLOTS.length; i++) {
            if (INNER_SLOTS[i + 1] == slot) return msgs.get(i).id();
        }
        return null;
    }
}