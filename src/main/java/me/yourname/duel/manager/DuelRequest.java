package me.yourname.duel.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.UUID;

public class DuelRequest {
    private final UUID senderUUID;

    public DuelRequest(Player sender) {
        this.senderUUID = sender.getUniqueId();
    }

    public Player getSender() {
        return Bukkit.getPlayer(senderUUID);
    }
}