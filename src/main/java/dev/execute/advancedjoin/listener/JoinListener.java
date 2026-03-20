package dev.execute.advancedjoin.listener;

import dev.execute.advancedjoin.AdvancedJoin;
import dev.execute.advancedjoin.manager.DatabaseManager;
import dev.execute.advancedjoin.model.PlayerData;
import dev.execute.advancedjoin.util.ColorUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinListener implements Listener {

    private final AdvancedJoin plugin;

    public JoinListener(AdvancedJoin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(null);

        plugin.getDatabaseManager().loadPlayer(player.getUniqueId(), data -> {
            if (!player.isOnline()) return;

            // Cooldown check
            boolean onCooldown = plugin.getCooldownManager().isOnCooldown(player.getUniqueId());
            plugin.getCooldownManager().recordJoin(player.getUniqueId());

            // VIP sanitize + defaults
            plugin.getVipManager().sanitize(player, data);
            if (player.hasPermission("advancedjoin.vip")) {
                plugin.getVipManager().applyVipDefaults(player, data);
            }

            // Streak + first join state
            boolean isFirst = data.isFirstJoin();
            data.updateStreak();
            data.setFirstJoin(false);
            plugin.getDatabaseManager().savePlayer(data);

            if (onCooldown) return;

            // 1. Join message
            if (isFirst) {
                String firstRaw = plugin.getLangManager().getString("join.first-join-message");
                firstRaw = plugin.getPlaceholderManager().replace(player, data, firstRaw);
                Component firstMsg = ColorUtil.parse(firstRaw);
                Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(firstMsg));
            } else if (data.isMessageEnabled()) {
                String raw = plugin.getMessageManager().resolveMessage(player, data);
                plugin.getAnimationManager().broadcast(player, data, raw);
            }

            // 2. Title (first join = special title, else regular)
            plugin.getWelcomeManager().showTitle(player, data, isFirst);

            // 3. Streak notification
            if (!isFirst && data.getStreak() > 1) {
                plugin.getWelcomeManager().showReturningMessage(player, data);
            }

            // 4. Sound
            plugin.getSoundManager().playJoinSound(player, data);

            // 5. Particles
            plugin.getParticleManager().spawnJoinParticle(player, data);

            // 6. Welcome screen (book/sign) — slight delay
            plugin.getWelcomeManager().showWelcomeScreen(player, data);

            // 7. Auto commands
            plugin.getWelcomeManager().runJoinCommands(player, data, isFirst);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.quitMessage(null);

        DatabaseManager db = plugin.getDatabaseManager();
        PlayerData data = db.getCached(player.getUniqueId());

        plugin.getQuitManager().broadcastQuit(player, data);

        if (data != null) {
            db.savePlayer(data);
            db.removeFromCache(player.getUniqueId());
        }

        plugin.getCooldownManager().remove(player.getUniqueId());
        plugin.getGuiManager().onQuit(player);
    }
}