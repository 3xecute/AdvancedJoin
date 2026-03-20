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

public class LangManager {

    private final AdvancedJoin plugin;
    private FileConfiguration lang;
    private String currentLang;

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
        if (!file.exists()) plugin.saveResource(langFile, false);

        lang = YamlConfiguration.loadConfiguration(file);

        InputStream defaultStream = plugin.getResource(langFile);
        if (defaultStream != null) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            lang.setDefaults(defaults);
        }

        plugin.getLogger().info("Language loaded: " + currentLang.toUpperCase());
    }

    public void reload() { load(); }

    /** Raw string with prefix replaced. */
    public String getRaw(String key) {
        String value = lang.getString(key, "&cMissing key: " + key);
        return value.replace("%prefix%", getRawPrefix());
    }

    /** Raw string with additional replacements. */
    public String getString(String key, String... replacements) {
        String raw = getRaw(key);
        return ColorUtil.replace(raw, replacements);
    }

    /** Parsed Component. */
    public Component get(String key, String... replacements) {
        return ColorUtil.parse(getString(key, replacements));
    }

    /** Raw string list (unparsed) — for GUI builders that call ColorUtil themselves. */
    public List<String> getRawList(String key) {
        String prefix = getRawPrefix();
        return lang.getStringList(key).stream()
                .map(l -> l.replace("%prefix%", prefix))
                .toList();
    }

    /** Parsed Component list. */
    public List<Component> getList(String key, String... replacements) {
        return getRawList(key).stream()
                .map(l -> ColorUtil.replace(l, replacements))
                .map(ColorUtil::parse)
                .toList();
    }

    private String getRawPrefix() {
        return lang.getString("prefix", "&8[&aAJ&8] ");
    }

    public String getCurrentLang() { return currentLang; }
}
