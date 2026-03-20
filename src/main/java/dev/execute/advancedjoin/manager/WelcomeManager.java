package dev.execute.advancedjoin.manager;

import dev.execute.advancedjoin.AdvancedJoin;
import dev.execute.advancedjoin.model.PlayerData;
import dev.execute.advancedjoin.util.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class WelcomeManager {

    private final AdvancedJoin plugin;

    public WelcomeManager(AdvancedJoin plugin) {
        this.plugin = plugin;
    }

    // ─── Welcome Screen ───────────────────────────────────────────────

    public void showWelcomeScreen(Player player, PlayerData data) {
        if (!plugin.getConfig().getBoolean("welcome-screen.enabled", true)) return;

        boolean firstOnly = plugin.getConfig().getBoolean("welcome-screen.first-join-only", false);
        if (firstOnly && !data.isFirstJoin()) return;

        String type = plugin.getConfig().getString("welcome-screen.type", "BOOK");

        // Delay slightly so inventory is ready
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            if (type.equalsIgnoreCase("BOOK")) {
                openBook(player, data);
            } else if (type.equalsIgnoreCase("SIGN")) {
                sendSignMessage(player, data);
            }
        }, 20L);
    }

    private void openBook(Player player, PlayerData data) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        if (meta == null) return;

        String title  = replace(plugin.getConfig().getString("welcome-screen.book.title", "Welcome!"), player, data);
        String author = plugin.getConfig().getString("welcome-screen.book.author", "Server");
        List<String> rawPages = plugin.getConfig().getStringList("welcome-screen.book.pages");

        meta.setTitle(title);
        meta.setAuthor(author);

        List<Component> pages = new ArrayList<>();
        for (String page : rawPages) {
            pages.add(ColorUtil.parse(replace(page, player, data)));
        }
        if (pages.isEmpty()) {
            pages.add(ColorUtil.parse("&#00ff88Welcome, &f" + player.getName() + "&a!"));
        }
        meta.pages(pages);
        book.setItemMeta(meta);

        player.openBook(book);
    }

    private void sendSignMessage(Player player, PlayerData data) {
        List<String> lines = plugin.getConfig().getStringList("welcome-screen.sign.lines");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(replace(line, player, data)).append("\n");
        }
        player.sendMessage(ColorUtil.parse(sb.toString().trim()));
    }

    // ─── Join Title ───────────────────────────────────────────────────

    public void showTitle(Player player, PlayerData data, boolean isFirst) {
        if (!plugin.getConfig().getBoolean("join-title.enabled", true)) return;

        boolean firstOnly = plugin.getConfig().getBoolean("join-title.first-join-only", false);
        if (firstOnly && !isFirst) return;

        String titleStr, subtitleStr;

        if (isFirst && plugin.getConfig().getBoolean("conditional-welcome.enabled", true)) {
            titleStr    = plugin.getConfig().getString("join-title.first-join.title",
                    "&#FFD700&l⭐ First Join!");
            subtitleStr = plugin.getConfig().getString("join-title.first-join.subtitle",
                    "&eWelcome, &f%player%&e!");
        } else {
            titleStr    = plugin.getConfig().getString("join-title.title", "&#00ff88&lWelcome");
            subtitleStr = plugin.getConfig().getString("join-title.subtitle", "&7Hello, &f%player%&7!");
        }

        Component title    = ColorUtil.parse(replace(titleStr, player, data));
        Component subtitle = ColorUtil.parse(replace(subtitleStr, player, data));

        int fadeIn  = plugin.getConfig().getInt("join-title.fade-in", 10);
        int stay    = plugin.getConfig().getInt("join-title.stay", 40);
        int fadeOut = plugin.getConfig().getInt("join-title.fade-out", 10);

        Title.Times times = Title.Times.times(
                Duration.ofMillis(fadeIn  * 50L),
                Duration.ofMillis(stay    * 50L),
                Duration.ofMillis(fadeOut * 50L)
        );

        player.showTitle(Title.title(title, subtitle, times));
    }

    // ─── Returning Player ─────────────────────────────────────────────

    public void showReturningMessage(Player player, PlayerData data) {
        if (!plugin.getConfig().getBoolean("conditional-welcome.enabled", true)) return;
        if (data.getStreak() <= 1) return;

        String msg = plugin.getConfig().getString(
                "conditional-welcome.returning-player.message",
                "&#00ff88Welcome back, &f%player%&a!");
        player.sendMessage(ColorUtil.parse(replace(msg, player, data)));
    }

    // ─── Auto Commands ────────────────────────────────────────────────

    public void runJoinCommands(Player player, PlayerData data, boolean isFirst) {
        // Regular join commands
        if (plugin.getConfig().getBoolean("join-commands.enabled", true)) {
            List<String> cmds = plugin.getConfig().getStringList("join-commands.commands");
            runCommands(player, data, cmds);
        }

        // First join commands
        if (isFirst && plugin.getConfig().getBoolean("first-join-commands.enabled", true)) {
            List<String> cmds = plugin.getConfig().getStringList("first-join-commands.commands");
            runCommands(player, data, cmds);
        }
    }

    private void runCommands(Player player, PlayerData data, List<String> commands) {
        for (String cmd : commands) {
            if (cmd == null || cmd.isBlank()) continue;
            String resolved = replace(cmd.trim(), player, data);

            if (resolved.toLowerCase().startsWith("console:")) {
                // Run as console
                String consoleCmd = resolved.substring("console:".length()).trim();
                Bukkit.getScheduler().runTask(plugin, () ->
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), consoleCmd));
            } else {
                // Run as player
                String playerCmd = resolved.startsWith("/") ? resolved.substring(1) : resolved;
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.performCommand(playerCmd));
            }
        }
    }

    // ─── Util ─────────────────────────────────────────────────────────

    private String replace(String text, Player player, PlayerData data) {
        return plugin.getPlaceholderManager().replace(player, data, text);
    }
}