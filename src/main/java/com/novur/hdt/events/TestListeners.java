package com.novur.hdt.events;

import com.novur.hdt.DataTest;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

public class TestListeners implements Listener {
    private final DataTest dataTest;
    public TestListeners(DataTest dataTest) {
        this.dataTest = dataTest;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if(dataTest.getConnectionPool().isConnected()) dataTest.getDatabase().createPlayer(event.getPlayer());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) throws ExecutionException, InterruptedException {
        if(!dataTest.getConnectionPool().isConnected()) return;

        Player player = event.getPlayer();

        int pointsToAdd = ThreadLocalRandom.current().nextInt(5);
        int currentPoints = dataTest.getDatabase().getPoints(player);

        player.sendMessage(ChatColor.GREEN + "Current points: " + currentPoints);
        player.sendMessage(ChatColor.GREEN + "Points to add: " + pointsToAdd);

        dataTest.getDatabase().addPoints(player,pointsToAdd);

        new BukkitRunnable() {
            @Override
            public void run() {
                int newPoints = 0;
                try {
                    newPoints = dataTest.getDatabase().getPoints(player);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                player.sendMessage(ChatColor.YELLOW + "New points: " + newPoints);
                player.sendMessage("");
            }
        }.runTaskLaterAsynchronously(dataTest, 2L);
    }
}
