package com.upgrade.globalmarket.internal.listen.impl;

import com.upgrade.globalmarket.internal.plugin.GlobalMarket;
import com.upgrade.globalmarket.internal.listen.AbstractListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Blank038
 * @date 2021/03/05
 */
public class PlayerCommonListener extends AbstractListener {

    /**
     * Server?     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        GlobalMarket.getStorageHandler().getOrLoadPlayerCache(player.getUniqueId(), false);
    }

    /**
     * Server?     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            GlobalMarket.getStorageHandler().savePlayerData(uuid, true);
            return true;
        });
        future.exceptionally((e) -> false);
        future.thenAccept((v) -> {
            if (v) {
                GlobalMarket.getStorageHandler().setLock(uuid, false);
            }
        });
    }
}
