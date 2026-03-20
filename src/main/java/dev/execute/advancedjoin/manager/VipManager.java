package dev.execute.advancedjoin.manager;

import dev.execute.advancedjoin.AdvancedJoin;
import dev.execute.advancedjoin.model.PlayerData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class VipManager {

    private final AdvancedJoin plugin;

    public VipManager(AdvancedJoin plugin) {
        this.plugin = plugin;
    }

    public boolean isVip(Player player) {
        return player.hasPermission("advancedjoin.vip");
    }

    /**
     * On first join or when player gains VIP, apply VIP defaults.
     */
    public void applyVipDefaults(Player player, PlayerData data) {
        if (!isVip(player)) return;

        // Only override if still on generic defaults
        if (data.getMessageId().equals("default")) {
            List<ConfigManager.MessageData> vipMessages = getVipMessages();
            if (!vipMessages.isEmpty()) {
                data.setMessageId(vipMessages.get(0).id());
            }
        }
        if (data.getAnimationId().equals("NONE")) {
            String anim = plugin.getConfig().getString("vip.default-animation", "GRADIENT_FLOW");
            data.setAnimationId(anim);
            data.setAnimationEnabled(true);
        }
        if (data.getParticleId().equals("heart")) {
            String particle = plugin.getConfig().getString("vip.default-particle", "end_rod");
            data.setParticleId(particle);
        }
    }

    /**
     * Get the VIP badge prefix string (raw, unparsed).
     */
    public String getBadge() {
        return plugin.getConfig().getString("vip.badge", "&#FFD700&l[VIP] ");
    }

    /**
     * Load VIP-exclusive messages from config.
     */
    public List<ConfigManager.MessageData> getVipMessages() {
        List<ConfigManager.MessageData> result = new ArrayList<>();
        List<?> raw = plugin.getConfig().getList("vip.messages");
        if (raw == null) return result;

        for (Object entry : raw) {
            if (entry instanceof java.util.Map<?, ?> map) {
                String id = (String) map.get("id");
                String text = (String) map.get("text");
                if (id != null && text != null) {
                    result.add(new ConfigManager.MessageData(id, text, true));
                }
            }
        }
        return result;
    }

    /**
     * Check if a message ID belongs to VIP section.
     */
    public boolean isVipMessage(String messageId) {
        return getVipMessages().stream().anyMatch(m -> m.id().equals(messageId));
    }

    /**
     * If a non-VIP player has a VIP message set (e.g. lost permission),
     * reset them to default.
     */
    public void sanitize(Player player, PlayerData data) {
        if (!isVip(player) && isVipMessage(data.getMessageId())) {
            data.setMessageId("default");
        }
        // Also check category messages
        ConfigManager.MessageData msg = plugin.getConfigManager().findMessage(data.getMessageId());
        if (msg != null && msg.vip() && !isVip(player)) {
            data.setMessageId("default");
        }
    }
}
