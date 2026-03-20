package dev.execute.advancedjoin.listener;

import dev.execute.advancedjoin.AdvancedJoin;
import dev.execute.advancedjoin.gui.*;
import dev.execute.advancedjoin.manager.ConfigManager;
import dev.execute.advancedjoin.model.PlayerData;
import dev.execute.advancedjoin.util.ColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class GuiListener implements Listener {

    private final AdvancedJoin plugin;

    public GuiListener(AdvancedJoin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (plugin.getGuiManager().getOpenMenu(player) != null) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        GuiManager.MenuType menuType = plugin.getGuiManager().getOpenMenu(player);
        if (menuType == null) return;

        event.setCancelled(true);

        int invSize = event.getInventory().getSize();
        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= invSize) return;
        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getType().isAir()) return;

        // Back — always first (slot 49 is border row)
        if (rawSlot == 49) {
            handleBack(player, menuType);
            return;
        }

        if (GuiBuilder.isBorderSlot(rawSlot, invSize)) return;

        switch (menuType) {
            case MAIN         -> handleMain(player, rawSlot);
            case MESSAGES     -> handleMessages(player, rawSlot, event.getInventory());
            case CATEGORY     -> handleCategory(player, rawSlot);
            case SOUNDS       -> handleSounds(player, rawSlot, event.getClick());
            case PARTICLES    -> handleParticles(player, rawSlot);
            case SETTINGS     -> handleSettings(player, rawSlot);
            case ANIMATION    -> handleAnimation(player, rawSlot);
            case QUIT_MESSAGE -> handleQuitMessage(player, rawSlot);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) plugin.getGuiManager().onClose(player);
    }

    // ─── Back ─────────────────────────────────────────────────────────

    private void handleBack(Player player, GuiManager.MenuType menuType) {
        switch (menuType) {
            case MESSAGES     -> plugin.getGuiManager().openMain(player);
            case CATEGORY     -> plugin.getGuiManager().openMessages(player);
            case SOUNDS       -> plugin.getGuiManager().openMain(player);
            case PARTICLES    -> plugin.getGuiManager().openMain(player);
            case SETTINGS     -> plugin.getGuiManager().openMain(player);
            case ANIMATION    -> plugin.getGuiManager().openSettings(player);
            case QUIT_MESSAGE -> plugin.getGuiManager().openMain(player);
            default -> {}
        }
    }

    // ─── Main ─────────────────────────────────────────────────────────

    private void handleMain(Player player, int slot) {
        if (MainMenu.isMessagesSlot(slot))  { plugin.getGuiManager().openMessages(player);     return; }
        if (MainMenu.isSoundsSlot(slot))    { plugin.getGuiManager().openSounds(player);       return; }
        if (MainMenu.isParticlesSlot(slot)) { plugin.getGuiManager().openParticles(player);    return; }
        if (MainMenu.isQuitSlot(slot))      { plugin.getGuiManager().openQuitMessages(player); return; }
        if (MainMenu.isSettingsSlot(slot))  { plugin.getGuiManager().openSettings(player);     }
    }

    // ─── Messages ─────────────────────────────────────────────────────

    private void handleMessages(Player player, int slot, Inventory inv) {
        String category = MessagesMenu.categoryAt(inv, slot, plugin);
        if (category == null) return;
        if (category.equals("VIP") && !player.hasPermission("advancedjoin.vip")) {
            player.sendMessage(plugin.getLangManager().get("no-permission"));
            return;
        }
        plugin.getGuiManager().openCategory(player, category);
    }

    // ─── Category ─────────────────────────────────────────────────────

    private void handleCategory(Player player, int slot) {
        String category = plugin.getGuiManager().getOpenCategory(player);
        if (category == null) return;

        String msgId = CategoryMenu.messageIdAt(slot, category, plugin);
        if (msgId == null) return;

        ConfigManager.MessageData msg = plugin.getConfigManager().findMessage(msgId);
        if (msg == null) msg = plugin.getVipManager().getVipMessages().stream()
                .filter(m -> m.id().equals(msgId)).findFirst().orElse(null);

        if (msg != null && msg.vip() && !player.hasPermission("advancedjoin.vip")) {
            player.sendMessage(plugin.getLangManager().get("no-permission"));
            return;
        }

        PlayerData data = plugin.getDatabaseManager().getCached(player.getUniqueId());
        if (data == null) return;

        data.setMessageId(msgId);
        plugin.getDatabaseManager().savePlayer(data);
        player.sendMessage(plugin.getLangManager().get("select.message", "%value%", msgId));
        plugin.getGuiManager().refresh(player);
    }

    // ─── Sounds ───────────────────────────────────────────────────────

    private void handleSounds(Player player, int slot, ClickType click) {
        String soundId = SoundsMenu.soundIdAt(slot, plugin);
        if (soundId == null) return;

        if (click == ClickType.RIGHT) {
            plugin.getSoundManager().preview(player, soundId);
            return;
        }

        PlayerData data = plugin.getDatabaseManager().getCached(player.getUniqueId());
        if (data == null) return;

        data.setSoundId(soundId);
        plugin.getDatabaseManager().savePlayer(data);
        player.sendMessage(plugin.getLangManager().get("select.sound", "%value%", soundId));
        plugin.getSoundManager().preview(player, soundId);
        plugin.getGuiManager().refresh(player);
    }

    // ─── Particles ────────────────────────────────────────────────────

    private void handleParticles(Player player, int slot) {
        String particleId = ParticlesMenu.particleIdAt(slot, plugin);
        if (particleId == null) return;

        PlayerData data = plugin.getDatabaseManager().getCached(player.getUniqueId());
        if (data == null) return;

        data.setParticleId(particleId);
        plugin.getDatabaseManager().savePlayer(data);
        player.sendMessage(plugin.getLangManager().get("select.particle", "%value%", particleId));
        plugin.getGuiManager().refresh(player);
    }

    // ─── Settings ─────────────────────────────────────────────────────

    private void handleSettings(Player player, int slot) {
        PlayerData data = plugin.getDatabaseManager().getCached(player.getUniqueId());
        if (data == null) return;

        if (slot == SettingsMenu.SLOT_TOGGLE_MESSAGE) {
            boolean on = data.toggleMessage();
            plugin.getDatabaseManager().savePlayer(data);
            player.sendMessage(plugin.getLangManager().get(on ? "toggle.message-on" : "toggle.message-off"));
            plugin.getGuiManager().refresh(player);

        } else if (slot == SettingsMenu.SLOT_TOGGLE_SOUND) {
            boolean on = data.toggleSound();
            plugin.getDatabaseManager().savePlayer(data);
            player.sendMessage(plugin.getLangManager().get(on ? "toggle.sound-on" : "toggle.sound-off"));
            plugin.getGuiManager().refresh(player);

        } else if (slot == SettingsMenu.SLOT_TOGGLE_PARTICLE) {
            boolean on = data.toggleParticle();
            plugin.getDatabaseManager().savePlayer(data);
            player.sendMessage(plugin.getLangManager().get(on ? "toggle.particle-on" : "toggle.particle-off"));
            plugin.getGuiManager().refresh(player);

        } else if (slot == SettingsMenu.SLOT_TOGGLE_ANIMATION) {
            boolean on = data.toggleAnimation();
            plugin.getDatabaseManager().savePlayer(data);
            player.sendMessage(plugin.getLangManager().get(on ? "toggle.animation-on" : "toggle.animation-off"));
            plugin.getGuiManager().refresh(player);

        } else if (slot == SettingsMenu.SLOT_TOGGLE_QUIT) {
            boolean on = data.toggleQuitMessage();
            plugin.getDatabaseManager().savePlayer(data);
            player.sendMessage(ColorUtil.parse(on ? "&aQuit message &fenabled&a." : "&cQuit message &fdisabled&c."));
            plugin.getGuiManager().refresh(player);

        } else if (slot == SettingsMenu.SLOT_OPEN_ANIMATION) {
            plugin.getGuiManager().openAnimation(player);

        } else if (slot == SettingsMenu.SLOT_OPEN_QUIT_MSG) {
            plugin.getGuiManager().openQuitMessages(player);

        } else if (slot == SettingsMenu.SLOT_TOGGLE_WELCOME) {
            // Admin only
            if (!player.hasPermission("advancedjoin.admin")) {
                player.sendMessage(plugin.getLangManager().get("no-permission"));
                return;
            }
            boolean current = plugin.getConfig().getBoolean("welcome-screen.enabled", true);
            plugin.getConfig().set("welcome-screen.enabled", !current);
            plugin.saveConfig();
            player.sendMessage(ColorUtil.parse("&#00ff88Welcome screen " + (!current ? "&aenabled" : "&cdisabled") + "&a."));
            plugin.getGuiManager().refresh(player);

        } else if (slot == SettingsMenu.SLOT_PREVIEW) {
            String raw = plugin.getMessageManager().resolveMessage(player, data);
            player.sendActionBar(ColorUtil.parse(raw));
            player.sendMessage(ColorUtil.parse("&#00ff88Previewing your join message on your action bar."));
        }
    }

    // ─── Animation ────────────────────────────────────────────────────

    private void handleAnimation(Player player, int slot) {
        String animId = AnimationMenu.animationIdAt(slot, plugin);
        if (animId == null) return;

        PlayerData data = plugin.getDatabaseManager().getCached(player.getUniqueId());
        if (data == null) return;

        data.setAnimationId(animId);
        plugin.getDatabaseManager().savePlayer(data);
        player.sendMessage(ColorUtil.parse("&#00ff88Animation set to &f" + animId + "&a."));
        plugin.getGuiManager().refresh(player);
    }

    // ─── Quit Message ─────────────────────────────────────────────────

    private void handleQuitMessage(Player player, int slot) {
        String msgId = QuitMessageMenu.messageIdAt(slot, plugin);
        if (msgId == null) return;

        PlayerData data = plugin.getDatabaseManager().getCached(player.getUniqueId());
        if (data == null) return;

        data.setQuitMessageId(msgId);
        plugin.getDatabaseManager().savePlayer(data);
        player.sendMessage(ColorUtil.parse("&#00ff88Quit message set to &f" + msgId + "&a."));
        plugin.getGuiManager().refresh(player);
    }
}