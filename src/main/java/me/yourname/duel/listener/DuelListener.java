package me.yourname.duel.listener;

import me.yourname.duel.DuelPlugin;
import me.yourname.duel.manager.DuelManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DuelListener implements Listener {
    private final DuelPlugin plugin;

    public DuelListener(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player deadPlayer = event.getEntity();
        DuelManager manager = plugin.getDuelManager();

        if (manager.isInDuel(deadPlayer)) {
            Player killer = deadPlayer.getKiller();
            if (killer != null && manager.isInDuel(killer)) {
                // 死亡后传送回初始位置
                manager.endDuel(killer, deadPlayer, null);
            } else {
                manager.endDuel(null, deadPlayer, null);
            }
        }
    }
}