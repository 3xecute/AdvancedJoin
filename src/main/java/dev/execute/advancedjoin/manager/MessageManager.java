package dev.execute.advancedjoin.manager;

import dev.execute.advancedjoin.AdvancedJoin;
import dev.execute.advancedjoin.model.PlayerData;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class MessageManager {

    private final AdvancedJoin plugin;

    public MessageManager(AdvancedJoin plugin) {
        this.plugin = plugin;
    }

    public String resolveMessage(Player player, PlayerData data) {
        ConfigManager cfg = plugin.getConfigManager();
        String messageId = data.getMessageId();

        String raw;
        if (messageId == null || messageId.equals("default")) {
            raw = plugin.getLangManager().getString("join.default-message");
        } else {
            // Check VIP messages first
            ConfigManager.MessageData vipMsg = plugin.getVipManager().getVipMessages().stream()
                    .filter(m -> m.id().equals(messageId)).findFirst().orElse(null);

            if (vipMsg != null) {
                if (!player.hasPermission("advancedjoin.vip")) {
                    raw = plugin.getLangManager().getString("join.default-message");
                } else {
                    raw = vipMsg.text();
                }
            } else {
                ConfigManager.MessageData msg = cfg.findMessage(messageId);
                if (msg == null) {
                    raw = plugin.getLangManager().getString("join.default-message");
                } else if (msg.vip() && !player.hasPermission("advancedjoin.vip")) {
                    raw = plugin.getLangManager().getString("join.default-message");
                } else {
                    raw = msg.text();
                }
            }
        }

        // Apply placeholders
        return plugin.getPlaceholderManager().replace(player, data, raw);
    }

    public Map<String, ConfigManager.CategoryData> getVisibleCategories(Player player) {
        return plugin.getConfigManager().getCategories();
    }

    public boolean canUse(Player player, String messageId) {
        if (messageId == null || messageId.equals("default")) return true;
        ConfigManager.MessageData msg = plugin.getConfigManager().findMessage(messageId);
        if (msg != null) return !msg.vip() || player.hasPermission("advancedjoin.vip");
        // Check VIP messages
        return plugin.getVipManager().getVipMessages().stream()
                .filter(m -> m.id().equals(messageId))
                .allMatch(m -> player.hasPermission("advancedjoin.vip"));
    }
}
