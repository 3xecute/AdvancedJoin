package dev.execute.advancedjoin.manager;

import dev.execute.advancedjoin.AdvancedJoin;
import dev.execute.advancedjoin.model.PlayerData;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class SoundManager {

    private final AdvancedJoin plugin;

    public SoundManager(AdvancedJoin plugin) {
        this.plugin = plugin;
    }

    public void playJoinSound(Player player, PlayerData data) {
        if (!data.isSoundEnabled()) return;
        playById(player, data.getSoundId());
    }

    public void playById(Player player, String soundId) {
        ConfigManager.SoundData sd = plugin.getConfigManager().findSound(soundId);
        if (sd == null || sd.sound().equalsIgnoreCase("NONE")) return;
        play(player, sd.sound(),
                (float) plugin.getConfig().getDouble("defaults.sound-volume", 1.0),
                (float) plugin.getConfig().getDouble("defaults.sound-pitch", 1.0));
    }

    public void preview(Player player, String soundId) {
        playById(player, soundId);
    }

    private void play(Player player, String soundName, float volume, float pitch) {
        try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING, "Unknown sound: " + soundName);
        }
    }
}
