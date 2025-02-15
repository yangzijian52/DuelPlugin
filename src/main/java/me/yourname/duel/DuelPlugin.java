package me.yourname.duel;

import me.yourname.duel.command.DuelCommand;
import me.yourname.duel.listener.DuelListener;
import me.yourname.duel.manager.DuelManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class DuelPlugin extends JavaPlugin {
    private DuelManager duelManager;

    @Override
    public void onEnable() {
        this.duelManager = new DuelManager(this);
        getCommand("dt").setExecutor(new DuelCommand(this));
        getServer().getPluginManager().registerEvents(new DuelListener(this), this);
        getLogger().info("§a单挑插件已加载！");
    }

    public DuelManager getDuelManager() {
        return duelManager;
    }
}