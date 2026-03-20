package dev.execute.advancedjoin.manager;

import dev.execute.advancedjoin.AdvancedJoin;
import dev.execute.advancedjoin.model.PlayerData;
import dev.execute.advancedjoin.util.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AnimationManager {

    public enum AnimationType { NONE, TYPEWRITER, GRADIENT_FLOW, PULSE }

    private final AdvancedJoin plugin;
    private final Map<UUID, Integer> activeTasks = new HashMap<>();

    public AnimationManager(AdvancedJoin plugin) {
        this.plugin = plugin;
    }

    /**
     * Broadcast join message.
     * NONE / disabled  → instant broadcast to chat (all players).
     * Animated          → broadcast final message to chat instantly,
     *                     then show animation on ACTION BAR for joining player only.
     */
    public void broadcast(Player joiningPlayer, PlayerData data, String rawMessage) {
        String finalRaw = rawMessage
                .replace("%player%", joiningPlayer.getName())
                .replace("%streak%", String.valueOf(data.getStreak()));

        // Always broadcast final colored message to chat immediately
        Component chatMsg = ColorUtil.parse(finalRaw);
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(chatMsg));

        // If animation enabled, also play it on ACTION BAR for joining player
        if (!data.isAnimationEnabled() || data.getAnimationId().equalsIgnoreCase("NONE")) return;

        int speed = getSpeed(data.getAnimationId());
        switch (parseType(data.getAnimationId())) {
            case TYPEWRITER    -> typewriter(joiningPlayer, finalRaw, speed);
            case GRADIENT_FLOW -> gradientFlow(joiningPlayer, finalRaw, speed);
            case PULSE         -> pulse(joiningPlayer, finalRaw, speed);
            default -> {}
        }
    }

    // ─── Typewriter — action bar ──────────────────────────────────────

    private void typewriter(Player player, String raw, int speed) {
        String plain = PlainTextComponentSerializer.plainText()
                .serialize(ColorUtil.parse(raw));
        int[] step = {0};

        int taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || step[0] > plain.length()) {
                cancelTask(player.getUniqueId());
                return;
            }
            String partial = plain.substring(0, step[0]);
            player.sendActionBar(Component.text("§a" + partial));
            step[0] += 2;
        }, 0L, Math.max(1, speed)).getTaskId();

        activeTasks.put(player.getUniqueId(), taskId);
    }

    // ─── Gradient Flow — action bar ───────────────────────────────────

    private void gradientFlow(Player player, String raw, int speed) {
        String plain = PlainTextComponentSerializer.plainText()
                .serialize(ColorUtil.parse(raw));
        int len = Math.max(1, plain.length());
        int[] offset = {0};
        int[] frame  = {0};
        int totalFrames = 24;

        int taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || frame[0] >= totalFrames) {
                // Show final colored version on action bar then clear
                player.sendActionBar(ColorUtil.parse(raw));
                cancelTask(player.getUniqueId());
                return;
            }
            net.kyori.adventure.text.TextComponent.Builder builder = Component.text();
            for (int i = 0; i < len; i++) {
                float t = ((float)(i + offset[0]) % len) / len;
                int g = (int)(200 + 55 * Math.sin(t * 2 * Math.PI));
                int b = (int)(100 + 155 * Math.sin(t * 2 * Math.PI + Math.PI / 2));
                builder.append(Component.text(String.valueOf(plain.charAt(i)))
                        .color(TextColor.color(0, g, b)));
            }
            player.sendActionBar(builder.build());
            offset[0] = (offset[0] + 3) % len;
            frame[0]++;
        }, 0L, Math.max(1, speed)).getTaskId();

        activeTasks.put(player.getUniqueId(), taskId);
    }

    // ─── Pulse — action bar ───────────────────────────────────────────

    private void pulse(Player player, String raw, int speed) {
        String plain = PlainTextComponentSerializer.plainText()
                .serialize(ColorUtil.parse(raw));
        int[] frame = {0};
        int totalFrames = 14;

        int taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || frame[0] >= totalFrames) {
                player.sendActionBar(ColorUtil.parse(raw));
                cancelTask(player.getUniqueId());
                return;
            }
            boolean bright = frame[0] % 2 == 0;
            TextColor color = bright ? TextColor.color(0x00ff88) : TextColor.color(0x005533);
            player.sendActionBar(Component.text(plain).color(color));
            frame[0]++;
        }, 0L, Math.max(1, speed)).getTaskId();

        activeTasks.put(player.getUniqueId(), taskId);
    }

    // ─── Helpers ──────────────────────────────────────────────────────

    private void cancelTask(UUID uuid) {
        Integer id = activeTasks.remove(uuid);
        if (id != null) Bukkit.getScheduler().cancelTask(id);
    }

    public void cancelAll() {
        activeTasks.values().forEach(Bukkit.getScheduler()::cancelTask);
        activeTasks.clear();
    }

    private AnimationType parseType(String id) {
        try { return AnimationType.valueOf(id.toUpperCase()); }
        catch (IllegalArgumentException e) { return AnimationType.NONE; }
    }

    private int getSpeed(String animationId) {
        return plugin.getConfigManager().getAnimations().stream()
                .filter(a -> a.id().equalsIgnoreCase(animationId))
                .mapToInt(ConfigManager.AnimationData::speed)
                .findFirst().orElse(2);
    }
}
