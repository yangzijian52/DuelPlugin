package me.yourname.duel.manager;

import me.yourname.duel.DuelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import java.util.*;

public class DuelManager {
    private final DuelPlugin plugin;
    private final Queue<DuelRequest> queue = new LinkedList<>();
    private final Map<Player, Location> originalLocations = new HashMap<>();
    private final Map<DuelRequest, BukkitTask> matchTimeoutTasks = new HashMap<>();
    private final Map<DuelRequest, BukkitTask> duelTimeoutTasks = new HashMap<>();
    private final Set<Player> inDuel = new HashSet<>();

    public DuelManager(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    public void handleDuelRequest(Player player) {
        // 检查是否已经在决斗中
        if (inDuel.contains(player)) {
            player.sendMessage("§c你已经在决斗中了！");
            return;
        }

        // 检查是否有其他玩家正在决斗
        if (!inDuel.isEmpty()) {
            player.sendMessage("§c当前有其他玩家正在决斗，请稍后再试！");
            return;
        }

        // 检查是否尝试和自己单挑
        if (queue.stream().anyMatch(req -> req.getSender().equals(player))) {
            player.sendMessage("§c你不能和自己单挑！");
            return;
        }

        DuelRequest request = new DuelRequest(player);
        queue.add(request);
        originalLocations.put(player, player.getLocation());
        startMatchTimeout(request); // 匹配5分钟超时

        player.sendMessage("§a已加入匹配队列，等待对手...");
        checkMatch();
    }

    private void checkMatch() {
        if (queue.size() >= 2) {
            DuelRequest req1 = queue.poll();
            DuelRequest req2 = queue.poll();
            Player p1 = req1.getSender();
            Player p2 = req2.getSender();

            if (p1 != null && p2 != null && p1.isOnline() && p2.isOnline()) {
                startDuel(p1, p2);
                cancelMatchTimeout(req1);
                cancelMatchTimeout(req2);
            }
        }
    }

    private void startDuel(Player p1, Player p2) {
        inDuel.add(p1);
        inDuel.add(p2);

        teleportToArena(p1, "a");
        teleportToArena(p2, "b");

        p1.sendMessage("§6决斗开始！对手: " + p2.getName());
        p2.sendMessage("§6决斗开始！对手: " + p1.getName());

        // 启动10分钟决斗超时
        DuelRequest duel = new DuelRequest(p1);
        startDuelTimeout(duel);
    }

    public void cancelMatch(Player player) {
        Iterator<DuelRequest> iterator = queue.iterator();
        while (iterator.hasNext()) {
            DuelRequest req = iterator.next();
            if (req.getSender().equals(player)) {
                iterator.remove();
                originalLocations.remove(player);
                cancelMatchTimeout(req);
                player.sendMessage("§a已取消匹配");
                return;
            }
        }
    }

    // 匹配超时（5分钟）
    private void startMatchTimeout(DuelRequest request) {
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (queue.remove(request)) {
                Player player = request.getSender();
                player.sendMessage("§c匹配超时，未找到对手");
                originalLocations.remove(player);
            }
        }, 20L * 60 * 5); // 5分钟
        matchTimeoutTasks.put(request, task);
    }

    // 决斗超时（10分钟）
    private void startDuelTimeout(DuelRequest duel) {
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player p1 = duel.getSender();
            Player p2 = getOpponent(p1);
            if (p1 != null && p2 != null) {
                endDuel(null, p1, "§6平局！双方未分胜负");
                endDuel(null, p2, "§6平局！双方未分胜负");
            }
        }, 20L * 60 * 10); // 10分钟
        duelTimeoutTasks.put(duel, task);
    }

    private Player getOpponent(Player player) {
        for (DuelRequest req : duelTimeoutTasks.keySet()) {
            if (req.getSender().equals(player)) {
                for (Player p : inDuel) {
                    if (!p.equals(player)) return p;
                }
            }
        }
        return null;
    }

    private void cancelMatchTimeout(DuelRequest request) {
        BukkitTask task = matchTimeoutTasks.remove(request);
        if (task != null) task.cancel();
    }

    public void endDuel(Player winner, Player loser, String message) {
        if (winner != null) {
            winner.sendMessage("§a胜利！你击败了 " + loser.getName());
            returnToOriginalLocation(winner);
        }
        if (loser != null) {
            loser.sendMessage(message != null ? message : "§c你被击败了！");
            returnToOriginalLocation(loser);
        }

        inDuel.remove(winner);
        inDuel.remove(loser);

        // 清理超时任务
        duelTimeoutTasks.entrySet().removeIf(entry -> {
            if (entry.getKey().getSender().equals(winner) || entry.getKey().getSender().equals(loser)) {
                entry.getValue().cancel();
                return true;
            }
            return false;
        });
    }

    private void returnToOriginalLocation(Player player) {
        Location loc = originalLocations.get(player);
        if (loc != null) {
            player.teleport(loc);
            originalLocations.remove(player);
        }
    }

    public boolean isInDuel(Player player) {
        return inDuel.contains(player);
    }

    private void teleportToArena(Player player, String warp) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "warp " + warp + " " + player.getName());
    }
}