package me.yourname.duel.command;

import me.yourname.duel.DuelPlugin;
import me.yourname.duel.manager.DuelManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelCommand implements CommandExecutor {
    private final DuelPlugin plugin;

    public DuelCommand(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c只有玩家可以使用此命令");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("q")) {
            plugin.getDuelManager().cancelMatch(player);
            return true;
        }

        if (plugin.getDuelManager().isInDuel(player)) {
            player.sendMessage("§c你已经在决斗中！");
            return true;
        }

        plugin.getDuelManager().handleDuelRequest(player);
        return true;
    }
}