package dev.execute.advancedjoin.manager;

import dev.execute.advancedjoin.AdvancedJoin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {

    private final AdvancedJoin plugin;
    // UUID → last join timestamp (ms)
    private final Map<UUID, Long> lastJoin = new ConcurrentHashMap<>();

    public CooldownManager(AdvancedJoin plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns true if the player is on cooldown (joined too recently).
     * Cooldown in seconds from config: join-cooldown-seconds (default 3).
     */
    public boolean isOnCooldown(UUID uuid) {
        int cooldownSec = plugin.getConfig().getInt("join-cooldown-seconds", 3);
        if (cooldownSec <= 0) return false;
        Long last = lastJoin.get(uuid);
        if (last == null) return false;
        return (System.currentTimeMillis() - last) < (cooldownSec * 1000L);
    }

    public void recordJoin(UUID uuid) {
        lastJoin.put(uuid, System.currentTimeMillis());
    }

    public void remove(UUID uuid) {
        lastJoin.remove(uuid);
    }
}
