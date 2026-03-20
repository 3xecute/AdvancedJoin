package dev.execute.advancedjoin.command;

import dev.execute.advancedjoin.AdvancedJoin;
import dev.execute.advancedjoin.model.PlayerData;
import dev.execute.advancedjoin.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AJoinCommand implements CommandExecutor, TabCompleter {

    private final AdvancedJoin plugin;

    public AJoinCommand(AdvancedJoin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLangManager().get("player-only"));
            return true;
        }

        if (!player.hasPermission("advancedjoin.use")) {
            player.sendMessage(plugin.getLangManager().get("no-permission"));
            return true;
        }

        // /ajoin reload
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("advancedjoin.admin")) {
                player.sendMessage(plugin.getLangManager().get("no-permission"));
                return true;
            }
            plugin.getConfigManager().load();
            plugin.getLangManager().reload();
            player.sendMessage(ColorUtil.parse("&#00ff88AdvancedJoin reloaded."));
            return true;
        }

        // /ajoin preview
        if (args.length == 1 && args[0].equalsIgnoreCase("preview")) {
            PlayerData data = plugin.getDatabaseManager().getCached(player.getUniqueId());
            if (data == null) {
                player.sendMessage(ColorUtil.parse("&cYour data is not loaded yet."));
                return true;
            }
            String raw = plugin.getMessageManager().resolveMessage(player, data);
            player.sendMessage(ColorUtil.parse("&#00ff88&lPreview: &r" + raw));
            player.sendActionBar(ColorUtil.parse(raw));
            return true;
        }

        // /ajoin streak
        if (args.length == 1 && args[0].equalsIgnoreCase("streak")) {
            PlayerData data = plugin.getDatabaseManager().getCached(player.getUniqueId());
            int streak = data != null ? data.getStreak() : 0;
            player.sendMessage(ColorUtil.parse("&#00ff88Your current streak: &f" + streak + " &7day(s)."));
            return true;
        }

        // Open main GUI
        plugin.getGuiManager().openMain(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("reload", "preview", "streak").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
