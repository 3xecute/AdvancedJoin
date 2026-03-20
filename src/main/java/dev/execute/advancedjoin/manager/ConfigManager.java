package dev.execute.advancedjoin.manager;

import dev.execute.advancedjoin.AdvancedJoin;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class ConfigManager {

    private final AdvancedJoin plugin;

    private final Map<String, CategoryData> categories   = new LinkedHashMap<>();
    private final List<SoundData>           sounds       = new ArrayList<>();
    private final List<ParticleData>        particles    = new ArrayList<>();
    private final List<AnimationData>       animations   = new ArrayList<>();
    private final List<MessageData>         quitMessages = new ArrayList<>();

    public ConfigManager(AdvancedJoin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        categories.clear();
        sounds.clear();
        particles.clear();
        animations.clear();
        quitMessages.clear();
        loadCategories();
        loadSounds();
        loadParticles();
        loadAnimations();
        loadQuitMessages();
    }

    // ─── Categories ───────────────────────────────────────────────────

    private void loadCategories() {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("categories");
        if (sec == null) return;
        for (String key : sec.getKeys(false)) {
            ConfigurationSection cat = sec.getConfigurationSection(key);
            if (cat == null) continue;
            String icon  = cat.getString("icon", "PAPER");
            String color = cat.getString("color", "#ffffff");
            List<MessageData> messages = new ArrayList<>();
            for (Object entry : cat.getList("messages", new ArrayList<>())) {
                if (entry instanceof Map<?, ?> map) {
                    String id   = (String) map.get("id");
                    String text = (String) map.get("text");
                    boolean vip = Boolean.TRUE.equals(map.get("vip"));
                    if (id != null && text != null) messages.add(new MessageData(id, text, vip));
                }
            }
            categories.put(key, new CategoryData(key, icon, color, messages));
        }
    }

    // ─── Sounds ───────────────────────────────────────────────────────

    private void loadSounds() {
        for (Map<?, ?> map : plugin.getConfig().getMapList("sounds")) {
            String id   = (String) map.get("id");
            String snd  = (String) map.get("sound");
            String desc = (String) map.get("description");
            if (id != null && snd != null) sounds.add(new SoundData(id, snd, desc != null ? desc : id));
        }
    }

    // ─── Particles ────────────────────────────────────────────────────

    private void loadParticles() {
        for (Map<?, ?> map : plugin.getConfig().getMapList("particles")) {
            String id   = (String) map.get("id");
            String type = (String) map.get("type");
            int count   = map.containsKey("count") ? (int) map.get("count") : 10;
            String desc = (String) map.get("description");
            if (id != null && type != null) particles.add(new ParticleData(id, type, count, desc != null ? desc : id));
        }
    }

    // ─── Animations ───────────────────────────────────────────────────

    private void loadAnimations() {
        for (Map<?, ?> map : plugin.getConfig().getMapList("animations")) {
            String id   = (String) map.get("id");
            String desc = (String) map.get("description");
            int speed   = map.containsKey("speed") ? (int) map.get("speed") : 2;
            if (id != null) animations.add(new AnimationData(id, desc != null ? desc : id, speed));
        }
    }

    // ─── Quit messages ────────────────────────────────────────────────

    private void loadQuitMessages() {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("quit-messages");
        if (sec == null) return;
        for (String key : sec.getKeys(false)) {
            String text = sec.getString(key);
            if (text != null) quitMessages.add(new MessageData(key, text, false));
        }
    }

    // ─── Finders ──────────────────────────────────────────────────────

    public MessageData findMessage(String id) {
        for (CategoryData cat : categories.values())
            for (MessageData m : cat.messages())
                if (m.id().equals(id)) return m;
        return null;
    }

    public SoundData findSound(String id) {
        return sounds.stream().filter(s -> s.id().equals(id)).findFirst().orElse(null);
    }

    public ParticleData findParticle(String id) {
        return particles.stream().filter(p -> p.id().equals(id)).findFirst().orElse(null);
    }

    public String findQuitMessage(String id) {
        return quitMessages.stream().filter(m -> m.id().equals(id))
                .map(MessageData::text).findFirst().orElse(null);
    }

    // ─── Defaults ─────────────────────────────────────────────────────

    public String getDefaultMessageId()   { return plugin.getConfig().getString("defaults.message", "default"); }
    public String getDefaultSoundId()     { return plugin.getConfig().getString("defaults.sound", "levelup"); }
    public String getDefaultParticleId()  { return plugin.getConfig().getString("defaults.particle", "heart"); }
    public String getDefaultAnimationId() { return plugin.getConfig().getString("defaults.animation", "NONE"); }

    // ─── Getters ──────────────────────────────────────────────────────

    public Map<String, CategoryData> getCategories()   { return categories; }
    public List<SoundData>           getSounds()        { return sounds; }
    public List<ParticleData>        getParticles()     { return particles; }
    public List<AnimationData>       getAnimations()    { return animations; }
    public List<MessageData>         getQuitMessages()  { return quitMessages; }

    // ─── Records ──────────────────────────────────────────────────────

    public record CategoryData(String name, String icon, String color, List<MessageData> messages) {}
    public record MessageData(String id, String text, boolean vip) {}
    public record SoundData(String id, String sound, String description) {}
    public record ParticleData(String id, String type, int count, String description) {}
    public record AnimationData(String id, String description, int speed) {}
}
