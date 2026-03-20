package dev.execute.advancedjoin.gui;

import dev.execute.advancedjoin.AdvancedJoin;
import dev.execute.advancedjoin.model.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SettingsMenu {

    /*
     * Row 2: 4 toggle yan yana        20,21,22,23
     * Row 3: quit + animation + title  29,31,33
     * Row 4: welcome + info + preview  37,40,43
     * Back:  49
     */

    public static final int SLOT_TOGGLE_MESSAGE   = 20;
    public static final int SLOT_TOGGLE_SOUND     = 21;
    public static final int SLOT_TOGGLE_PARTICLE  = 22;
    public static final int SLOT_TOGGLE_ANIMATION = 23;
    public static final int SLOT_TOGGLE_QUIT      = 29;
    public static final int SLOT_OPEN_ANIMATION   = 31;
    public static final int SLOT_OPEN_QUIT_MSG    = 33;
    public static final int SLOT_TOGGLE_WELCOME   = 37;
    public static final int SLOT_INFO             = 40;
    public static final int SLOT_PREVIEW          = 43;
    public static final int SLOT_BACK             = 49;

    public static Inventory build(AdvancedJoin plugin, Player player) {
        PlayerData data = plugin.getDatabaseManager().getCached(player.getUniqueId());
        Inventory inv = GuiBuilder.create(54,
                plugin.getLangManager().getString("menu.settings-title"));
        GuiBuilder.fillBorder(inv);
        if (data == null) return inv;

        // Row 2 — 4 toggle yan yana
        inv.setItem(SLOT_TOGGLE_MESSAGE,   toggle(Material.WRITABLE_BOOK,
                "&#00ff88&lJoin Message",   data.isMessageEnabled()));
        inv.setItem(SLOT_TOGGLE_SOUND,     toggle(Material.MUSIC_DISC_CREATOR,
                "&#00ff88&lJoin Sound",     data.isSoundEnabled()));
        inv.setItem(SLOT_TOGGLE_PARTICLE,  toggle(Material.DRAGON_BREATH,
                "&#00ff88&lJoin Particle",  data.isParticleEnabled()));
        inv.setItem(SLOT_TOGGLE_ANIMATION, toggle(Material.CLOCK,
                "&#00ff88&lJoin Animation", data.isAnimationEnabled()));

        // Row 3
        inv.setItem(SLOT_TOGGLE_QUIT, toggle(Material.MUSIC_DISC_PIGSTEP,
                "&#ff4444&lQuit Message", data.isQuitMessageEnabled()));

        inv.setItem(SLOT_OPEN_ANIMATION, GuiBuilder.buildItem(Material.NAUTILUS_SHELL,
                "&#00ff88&lAnimations",
                "&7Current: &f" + data.getAnimationId(),
                "",
                "&a▶ Click to browse"));

        inv.setItem(SLOT_OPEN_QUIT_MSG, GuiBuilder.buildItem(Material.PAPER,
                "&#ff4444&lQuit Messages",
                "&7Current: &f" + data.getQuitMessageId(),
                "",
                "&a▶ Click to browse"));

        // Row 4
        boolean welcomeEnabled = plugin.getConfig().getBoolean("welcome-screen.enabled", true);
        String welcomeType = plugin.getConfig().getString("welcome-screen.type", "BOOK");
        inv.setItem(SLOT_TOGGLE_WELCOME, GuiBuilder.buildItem(
                welcomeType.equalsIgnoreCase("BOOK") ? Material.WRITTEN_BOOK : Material.OAK_SIGN,
                "&#00ff88&lWelcome Screen",
                "&7Type: &f" + welcomeType,
                "&7Status: " + (welcomeEnabled ? "&#00ff88● ON" : "&c● OFF"),
                "",
                "&7&o(Admin config only)"));

        inv.setItem(SLOT_INFO, GuiBuilder.buildItem(Material.BOOK,
                "&#00ff88&lCurrent Settings",
                "&7Message:   &f" + data.getMessageId(),
                "&7Sound:     &f" + data.getSoundId(),
                "&7Particle:  &f" + data.getParticleId(),
                "&7Animation: &f" + data.getAnimationId(),
                "&7Quit Msg:  &f" + data.getQuitMessageId(),
                "&7Streak:    &f" + data.getStreak() + " days"));

        inv.setItem(SLOT_PREVIEW, GuiBuilder.buildItem(Material.ENDER_EYE,
                "&#00ff88&lPreview",
                "&7Click to preview your",
                "&7join message on action bar.",
                "",
                "&a▶ Click to preview"));

        inv.setItem(SLOT_BACK, GuiBuilder.navItem(Material.SPECTRAL_ARROW,
                "&c&l← Back", "&7Return to main menu."));

        return inv;
    }

    private static ItemStack toggle(Material mat, String name, boolean enabled) {
        String status = enabled ? "&#00ff88● ON" : "&c● OFF";
        String hint   = enabled ? "&7Click to &cdisable&7." : "&7Click to &aenable&7.";
        ItemStack item = GuiBuilder.buildItem(mat, name, "&7Status: " + status, "", hint);
        return enabled ? GuiBuilder.addGlow(item) : item;
    }

    public static boolean isBackSlot(int slot) { return slot == SLOT_BACK; }
}