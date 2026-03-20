package dev.execute.advancedjoin.manager;

import dev.execute.advancedjoin.AdvancedJoin;
import dev.execute.advancedjoin.model.PlayerData;
import dev.execute.advancedjoin.util.ColorUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class QuitManager {

    private final AdvancedJoin plugin;

    public QuitManager(AdvancedJoin plugin) {
        this.plugin = plugin;
    }

    public void broadcastQuit(Player player, PlayerData data) {
        if (data == null || !data.isQuitMessageEnabled()) return;

        String raw = resolveQuitMessage(player, data);
        raw = plugin.getPlaceholderManager().replace(player, data, raw);

        Component msg = ColorUtil.parse(raw);
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.equals(player))
                .forEach(p -> p.sendMessage(msg));
    }

    private String resolveQuitMessage(Player player, PlayerData data) {
        String id = data.getQuitMessageId();
        if (id == null || id.equals("default")) {
            return plugin.getLangManager().getString("quit.default-message");
        }

        // Look up in config quit messages
        String text = plugin.getConfigManager().findQuitMessage(id);
        if (text == null) {
            return plugin.getLangManager().getString("quit.default-message");
        }
        return text;
    }
}
