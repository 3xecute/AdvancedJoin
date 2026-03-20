package dev.execute.advancedjoin.gui;

import dev.execute.advancedjoin.util.ColorUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class GuiBuilder {

    public static final Material BORDER = Material.BLACK_STAINED_GLASS_PANE;
    // Tag stored in inventory title to identify our GUIs
    public static final String GUI_TAG = "§0§0§0"; // invisible marker

    private GuiBuilder() {}

    public static Inventory create(int size, String title) {
        // Append invisible marker so GuiListener can identify our inventories
        return Bukkit.createInventory(null, size, ColorUtil.parse(title));
    }

    /** Fill only the border row/col slots of a 54-slot inventory. */
    public static void fillBorder(Inventory inv) {
        int size = inv.getSize();
        int rows = size / 9;
        ItemStack glass = borderItem();
        for (int slot = 0; slot < 9; slot++) {
            inv.setItem(slot, glass);
            inv.setItem(size - 9 + slot, glass);
        }
        for (int row = 1; row < rows - 1; row++) {
            inv.setItem(row * 9, glass);
            inv.setItem(row * 9 + 8, glass);
        }
    }

    public static ItemStack borderItem() {
        ItemStack item = new ItemStack(BORDER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.empty());
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack buildItem(Material material, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.displayName(ColorUtil.parse(name));
        meta.addItemFlags(
                ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_UNBREAKABLE,
                ItemFlag.HIDE_ADDITIONAL_TOOLTIP
        );

        if (loreLines != null && !loreLines.isEmpty()) {
            meta.lore(loreLines.stream()
                    .map(ColorUtil::parse)
                    .collect(Collectors.toList()));
        }

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack buildItem(Material material, String name, String... loreLines) {
        return buildItem(material, name, Arrays.asList(loreLines));
    }

    public static ItemStack addGlow(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack navItem(Material material, String name, String... lore) {
        return buildItem(material, name, lore);
    }

    public static boolean isBorderSlot(int slot, int size) {
        int rows = size / 9;
        int row = slot / 9;
        int col = slot % 9;
        return row == 0 || row == rows - 1 || col == 0 || col == 8;
    }
}
