package com.upgrade.globalmarket.internal.listen.impl;

import com.upgrade.globalmarket.internal.plugin.GlobalMarket;
import com.upgrade.globalmarket.api.event.PlayerSaleEvent;
import com.upgrade.globalmarket.internal.cache.other.SaleLog;
import com.upgrade.globalmarket.internal.enums.LogType;
import com.upgrade.globalmarket.internal.listen.AbstractListener;
import org.bukkit.event.EventHandler;

/**
 * @author Blank038
 */
public class CoreListener extends AbstractListener {

    @EventHandler
    public void onSaleSell(PlayerSaleEvent.Sell.Post event) {
        SaleLog saleLog = SaleLog.builder()
                .logType(LogType.SELL)
                .sourceMarket(event.getMarketData().getSourceId())
                .triggerTime(System.currentTimeMillis())
                .saleCache(event.getSaleCache())
                .triggerPlayerUUID(event.getPlayer().getUniqueId())
                .build();
        GlobalMarket.getStorageHandler().addLog(saleLog);
    }

    @EventHandler
    public void onSaleBuy(PlayerSaleEvent.Buy event) {
        SaleLog saleLog = SaleLog.builder()
                .logType(LogType.BUY)
                .sourceMarket(event.getMarketData().getSourceId())
                .triggerTime(System.currentTimeMillis())
                .saleCache(event.getSaleCache())
                .triggerPlayerUUID(event.getPlayer().getUniqueId())
                .build();
        GlobalMarket.getStorageHandler().addLog(saleLog);
    }
}
