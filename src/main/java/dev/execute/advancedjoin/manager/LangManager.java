package dev.execute.advancedjoin.manager;

import dev.execute.advancedjoin.AdvancedJoin;
import dev.execute.advancedjoin.util.ColorUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class LangManager {

    private final AdvancedJoin plugin;
    private FileConfiguration lang;
    private FileConfiguration defaults;
    private String currentLang;

    // Hardcoded fallbacks — last resort if key missing from both file and jar
    private static final Map<String, String> FALLBACKS = Map.of(
            "join.default-message",      "&#00ff88[+] &f%player%",
            "join.first-join-message",   "&#00ff88&l★ &f%player% &ajoined for the first time! Welcome!",
            "join.streak-message",       "&#00ff88Welcome back! Your streak: &f%streak% &adays.",
            "quit.default-message",      "&8[&c-&8] &7%player% &8left the server.",
            "no-permission",             "&cYou don't have permission.",
            "player-only",               "&cThis command can only be used by players."
    );

    public LangManager(AdvancedJoin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        currentLang = plugin.getConfig().getString("language", "en");
        if (!currentLang.equals("en") && !currentLang.equals("tr")) {
            plugin.getLogger().warning("Unknown language '" + currentLang + "', falling back to 'en'.");
            currentLang = "en";
        }

        String langFile = "lang/" + currentLang + ".yml";
        File file = new File(plugin.getDataFolder(), langFile);

        // Always overwrite lang files with latest version from jar
        if (file.exists()) {
            file.delete();
        }
        plugin.saveResource(langFile, false);

        lang = YamlConfiguration.loadConfiguration(file);

        // Load defaults from jar for merging
        InputStream defaultStream = plugin.getResource(langFile);
        if (defaultStream != null) {
            defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            lang.setDefaults(defaults);
        }

        plugin.getLogger().info("Language loaded: " + currentLang.toUpperCase());
    }

    public void reload() { load(); }

    public String getRaw(String key) {
        // 1. Try loaded lang file
        String value = lang.getString(key);
        if (value != null) {
            return value.replace("%prefix%", getRawPrefix());
        }

        // 2. Try jar defaults
        if (defaults != null) {
            value = defaults.getString(key);
            if (value != null) {
                plugin.getLogger().warning("Lang key '" + key + "' missing from file, using jar default.");
                return value.replace("%prefix%", getRawPrefix());
            }
        }

        // 3. Hardcoded fallback
        if (FALLBACKS.containsKey(key)) {
            plugin.getLogger().warning("Lang key '" + key + "' missing entirely, using hardcoded fallback.");
            return FALLBACKS.get(key).replace("%prefix%", getRawPrefix());
        }

        // 4. Return empty string — never show "Missing key:" to players
        plugin.getLogger().warning("Lang key '" + key + "' not found anywhere!");
        return "";
    }

    public String getString(String key, String... replacements) {
        String raw = getRaw(key);
        return ColorUtil.replace(raw, replacements);
    }

    public Component get(String key, String... replacements) {
        String raw = getString(key, replacements);
        if (raw.isEmpty()) return Component.empty();
        return ColorUtil.parse(raw);
    }

    public List<String> getRawList(String key) {
        String prefix = getRawPrefix();
        List<String> list = lang.getStringList(key);
        if (list.isEmpty() && defaults != null) {
            list = defaults.getStringList(key);
        }
        return list.stream()
                .map(l -> l.replace("%prefix%", prefix))
                .toList();
    }

    public List<Component> getList(String key, String... replacements) {
        return getRawList(key).stream()
                .map(l -> ColorUtil.replace(l, replacements))
                .map(ColorUtil::parse)
                .toList();
    }

    private String getRawPrefix() {
        String prefix = lang.getString("prefix");
        if (prefix == null && defaults != null) prefix = defaults.getString("prefix");
        return prefix != null ? prefix : "&#00ff88&lAdvancedJoin &8» ";
    }

    public String getCurrentLang() { return currentLang; }
}
