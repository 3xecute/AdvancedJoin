package dev.execute.advancedjoin.manager;

import dev.execute.advancedjoin.AdvancedJoin;
import dev.execute.advancedjoin.model.PlayerData;
import org.bukkit.entity.Player;

public class PlaceholderManager {

    private final AdvancedJoin plugin;
    private boolean papiEnabled = false;

    public PlaceholderManager(AdvancedJoin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            papiEnabled = true;
            plugin.getLogger().info("PlaceholderAPI found — placeholders enabled.");
        }
    }

    public String replace(Player player, PlayerData data, String text) {
        // Built-in placeholders
        text = text
                .replace("%player%",     player.getName())
                .replace("%displayname%", player.getDisplayName())
                .replace("%world%",      player.getWorld().getName())
                .replace("%streak%",     String.valueOf(data.getStreak()))
                .replace("%online%",     String.valueOf(plugin.getServer().getOnlinePlayers().size()))
                .replace("%max_online%", String.valueOf(plugin.getServer().getMaxPlayers()));

        // PAPI — loaded via reflection to avoid hard dependency
        if (papiEnabled) {
            try {
                Class<?> papiClass = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                java.lang.reflect.Method method = papiClass.getMethod("setPlaceholders", Player.class, String.class);
                text = (String) method.invoke(null, player, text);
            } catch (Exception ignored) {}
        }

        return text;
    }

    public boolean isPapiEnabled() { return papiEnabled; }
}