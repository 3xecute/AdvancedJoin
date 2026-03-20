package dev.execute.advancedjoin.manager;

import dev.execute.advancedjoin.AdvancedJoin;
import dev.execute.advancedjoin.model.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class ParticleManager {

    public enum Shape { BURST, RING, SPIRAL }

    private final AdvancedJoin plugin;

    public ParticleManager(AdvancedJoin plugin) {
        this.plugin = plugin;
    }

    public void spawnJoinParticle(Player player, PlayerData data) {
        if (!data.isParticleEnabled()) return;
        ConfigManager.ParticleData pd = plugin.getConfigManager().findParticle(data.getParticleId());
        if (pd == null || pd.type().equalsIgnoreCase("NONE")) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            try {
                Particle particle = Particle.valueOf(pd.type());
                spawnShape(player, particle, pd.count(), Shape.BURST);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.WARNING, "Unknown particle: " + pd.type());
            }
        }, 5L);
    }

    public void spawnShape(Player player, Particle particle, int count, Shape shape) {
        Location base = player.getLocation().add(0, 1, 0);
        switch (shape) {
            case BURST  -> spawnBurst(player, particle, base, count);
            case RING   -> spawnRing(player, particle, base, count);
            case SPIRAL -> spawnSpiral(player, particle, base, count);
        }
    }

    private void spawnBurst(Player player, Particle particle, Location loc, int count) {
        player.getWorld().spawnParticle(particle, loc, count, 0.5, 0.5, 0.5, 0.02);
    }

    private void spawnRing(Player player, Particle particle, Location center, int count) {
        double radius = 1.0;
        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI / count) * i;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location loc = new Location(center.getWorld(), x, center.getY(), z);
            player.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0);
        }
    }

    private void spawnSpiral(Player player, Particle particle, Location base, int count) {
        int[] step = {0};
        int taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (step[0] >= count || !player.isOnline()) return;
            double angle = step[0] * 0.5;
            double radius = 0.5 + step[0] * 0.05;
            double x = base.getX() + radius * Math.cos(angle);
            double z = base.getZ() + radius * Math.sin(angle);
            double y = base.getY() + step[0] * 0.05;
            Location loc = new Location(base.getWorld(), x, y, z);
            player.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0);
            step[0]++;
        }, 0L, 1L).getTaskId();

        // Cancel after count ticks
        Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.getScheduler().cancelTask(taskId), count + 5L);
    }
}
