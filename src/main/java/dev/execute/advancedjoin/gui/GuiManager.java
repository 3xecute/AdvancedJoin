package dev.execute.advancedjoin.gui;

import dev.execute.advancedjoin.AdvancedJoin;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GuiManager {

    public enum MenuType {
        MAIN, MESSAGES, CATEGORY, SOUNDS, PARTICLES,
        SETTINGS, ANIMATION, QUIT_MESSAGE
    }

    private final Map<UUID, MenuType> openMenus    = new HashMap<>();
    private final Map<UUID, String>   openCategory = new HashMap<>();
    private final Set<UUID>           switching    = new HashSet<>();

    private final AdvancedJoin plugin;

    public GuiManager(AdvancedJoin plugin) {
        this.plugin = plugin;
    }

    public void openMain(Player player)         { open(player, MenuType.MAIN,         () -> MainMenu.build(plugin, player)); }
    public void openMessages(Player player)     { open(player, MenuType.MESSAGES,     () -> MessagesMenu.build(plugin, player)); }
    public void openSounds(Player player)       { open(player, MenuType.SOUNDS,       () -> SoundsMenu.build(plugin, player)); }
    public void openParticles(Player player)    { open(player, MenuType.PARTICLES,    () -> ParticlesMenu.build(plugin, player)); }
    public void openSettings(Player player)     { open(player, MenuType.SETTINGS,     () -> SettingsMenu.build(plugin, player)); }
    public void openAnimation(Player player)    { open(player, MenuType.ANIMATION,    () -> AnimationMenu.build(plugin, player)); }
    public void openQuitMessages(Player player) { open(player, MenuType.QUIT_MESSAGE, () -> QuitMessageMenu.build(plugin, player)); }

    public void openCategory(Player player, String categoryName) {
        switching.add(player.getUniqueId());
        openMenus.put(player.getUniqueId(), MenuType.CATEGORY);
        openCategory.put(player.getUniqueId(), categoryName);
        player.openInventory(CategoryMenu.build(plugin, player, categoryName));
        switching.remove(player.getUniqueId());
    }

    private void open(Player player, MenuType type, java.util.function.Supplier<org.bukkit.inventory.Inventory> builder) {
        switching.add(player.getUniqueId());
        openMenus.put(player.getUniqueId(), type);
        player.openInventory(builder.get());
        switching.remove(player.getUniqueId());
    }

    public void refresh(Player player) {
        MenuType type = openMenus.get(player.getUniqueId());
        if (type == null) return;
        switch (type) {
            case MAIN         -> openMain(player);
            case MESSAGES     -> openMessages(player);
            case CATEGORY     -> openCategory(player, openCategory.getOrDefault(player.getUniqueId(), ""));
            case SOUNDS       -> openSounds(player);
            case PARTICLES    -> openParticles(player);
            case SETTINGS     -> openSettings(player);
            case ANIMATION    -> openAnimation(player);
            case QUIT_MESSAGE -> openQuitMessages(player);
        }
    }

    public MenuType getOpenMenu(Player player)   { return openMenus.get(player.getUniqueId()); }
    public String getOpenCategory(Player player) { return openCategory.get(player.getUniqueId()); }
    public boolean isSwitching(Player player)    { return switching.contains(player.getUniqueId()); }

    public void onClose(Player player) {
        if (!switching.contains(player.getUniqueId())) {
            openMenus.remove(player.getUniqueId());
        }
    }

    public void onQuit(Player player) {
        openMenus.remove(player.getUniqueId());
        openCategory.remove(player.getUniqueId());
        switching.remove(player.getUniqueId());
    }
}
