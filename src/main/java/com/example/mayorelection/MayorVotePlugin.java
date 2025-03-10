package com.example.mayorelection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


// Plugin pour voter sur un serveur Minecraft, le plugin permet de voter pour un joueur, pour que celui ci devienne maire du serveur.
// @Smokss

public class MayorVotePlugin extends JavaPlugin implements Listener, TabExecutor {

    private final Map<Player, Player> votes = new HashMap<>();
    private final Map<Player, Integer> voteCounts = new HashMap<>();

    @Override
    public void onEnable() {
        Objects.requireNonNull(this.getCommand("vote")).setExecutor(this);
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("vote")) {
            if (sender instanceof Player) {
                Player voter = (Player) sender;
                if (args.length != 1) {
                    voter.sendMessage(ChatColor.RED + "Usage: /vote <player>");
                    return true;
                }
                Player candidate = Bukkit.getPlayer(args[0]);
                if (candidate == null || !candidate.isOnline()) {
                    voter.sendMessage(ChatColor.RED + "Joueur non trouvé ou non en ligne.");
                    return true;
                }
                votes.put(voter, candidate);
                voter.sendMessage(ChatColor.GREEN + "Vous avez voté pour " + candidate.getName());
                checkVotes();
                return true;
            }
        }
        return false;
    }

    private void checkVotes() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!votes.containsKey(player)) {
                return;
            }
        }
        voteCounts.clear();
        for (Player candidate : votes.values()) {
            voteCounts.put(candidate, voteCounts.getOrDefault(candidate, 0) + 1);
        }
        Player mayor = null;
        int maxVotes = 0;
        for (Map.Entry<Player, Integer> entry : voteCounts.entrySet()) {
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                mayor = entry.getKey();
            }
        }
        if (mayor != null) {
            announceMayor(mayor);
        }
    }

    private void announceMayor(Player mayor) {
        Bukkit.broadcastMessage(ChatColor.GOLD + "Le nouveau maire est : " + mayor.getName());
        new BukkitRunnable() {
            @Override
            public void run() {
                Location loc = mayor.getLocation();
                Firework firework = mayor.getWorld().spawn(loc, Firework.class);
                FireworkEffect.builder()
                        .withColor(org.bukkit.Color.RED)
                        .withColor(org.bukkit.Color.GREEN)
                        .withColor(org.bukkit.Color.BLUE)
                        .with(FireworkEffect.Type.BALL_LARGE)
                        .trail(true)
                        .flicker(true)
                        .build();
                firework.setFireworkMeta(firework.getFireworkMeta());
            }
        }.runTaskTimer(this, 0L, 20L * 15L);
    }
}
