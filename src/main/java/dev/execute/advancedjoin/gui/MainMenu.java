package dev.execute.advancedjoin.gui;

import dev.execute.advancedjoin.AdvancedJoin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class MainMenu {

    /*
     * 54 slots — 5 item yan yana ortalı row 2'de:
     *
     *  B B B B B B B B B   row 0
     *  B . . . . . . . B   row 1
     *  B . M S P Q T . B   row 2  slots 20,21,22,23,24
     *  B . . . . . . . B   row 3
     *  B . . . . . . . B   row 4
     *  B B B B B B B B B   row 5
     *
     *  Col 1=border, col 2=20, col 3=21, col 4=22, col 5=23, col 6=24, col 7=border
     *  Tam orta: row*9 + col → row2 = 18..26
     *  5 item: 20,21,22,23,24
     */

    private static final int SLOT_MESSAGES  = 20;
    private static final int SLOT_SOUNDS    = 21;
    private static final int SLOT_PARTICLES = 22;
    private static final int SLOT_QUIT      = 23;
    private static final int SLOT_SETTINGS  = 24;

    public static Inventory build(AdvancedJoin plugin, Player player) {
        Inventory inv = GuiBuilder.create(54,
                plugin.getLangManager().getString("menu.main-title"));
        GuiBuilder.fillBorder(inv);

        inv.setItem(SLOT_MESSAGES, GuiBuilder.buildItem(Material.WRITABLE_BOOK,
                "&#00ff88&lJoin Messages",
                "&7Customize your join message.",
                "&7Choose from multiple categories.",
                "",
                "&a▶ Click to open"));

        inv.setItem(SLOT_SOUNDS, GuiBuilder.buildItem(Material.MUSIC_DISC_CREATOR,
                "&#00ff88&lJoin Sounds",
                "&7Choose the sound that plays",
                "&7when you join the server.",
                "",
                "&a▶ Click to open"));

        inv.setItem(SLOT_PARTICLES, GuiBuilder.buildItem(Material.DRAGON_BREATH,
                "&#00ff88&lJoin Particles",
                "&7Select a particle effect",
                "&7that spawns around you on join.",
                "",
                "&a▶ Click to open"));

        inv.setItem(SLOT_QUIT, GuiBuilder.buildItem(Material.MUSIC_DISC_PIGSTEP,
                "&#ff4444&lQuit Messages",
                "&7Customize your quit message.",
                "",
                "&a▶ Click to open"));

        inv.setItem(SLOT_SETTINGS, GuiBuilder.buildItem(Material.COMPARATOR,
                "&#00ff88&lSettings",
                "&7Toggle features on or off.",
                "&7Animations, preview & more.",
                "",
                "&a▶ Click to open"));

        return inv;
    }

    public static boolean isMessagesSlot(int slot)  { return slot == SLOT_MESSAGES; }
    public static boolean isSoundsSlot(int slot)    { return slot == SLOT_SOUNDS; }
    public static boolean isParticlesSlot(int slot) { return slot == SLOT_PARTICLES; }
    public static boolean isQuitSlot(int slot)      { return slot == SLOT_QUIT; }
    public static boolean isSettingsSlot(int slot)  { return slot == SLOT_SETTINGS; }
}