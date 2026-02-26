package com.upgrade.globalmarket.internal.task.impl;

import com.upgrade.globalmarket.api.GlobalMarketApi;
import com.upgrade.globalmarket.api.entity.MarketData;
import com.upgrade.globalmarket.internal.config.GeneralOption;
import com.upgrade.globalmarket.internal.data.DataContainer;
import com.upgrade.globalmarket.internal.economy.BaseEconomy;
import com.upgrade.globalmarket.internal.i18n.I18n;
import com.upgrade.globalmarket.internal.plugin.GlobalMarket;
import com.upgrade.globalmarket.internal.task.AbstractTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

/**
 * @author Blank038
 */
public class OfflineTransactionTask extends AbstractTask {

    @Override
    public synchronized void run() {
        Bukkit.getOnlinePlayers().forEach(this::checkResult);
    }

    private void checkResult(Player player) {
        GlobalMarket.getStorageHandler().getOfflineTransactionByPlayer(player.getUniqueId()).forEach((k, v) -> {
            if (GlobalMarket.getStorageHandler().removeOfflineTransaction(k)) {
                // Get Market Data
                MarketData marketData = DataContainer.MARKET_DATA.getOrDefault(v.getSourceMarket(), null);
                // Get Obtain
                double price = v.getAmount(), tax = 0;
                if (marketData != null) {
                    tax = price * marketData.getPermsValueForPlayer(marketData.getTaxSection(), player, false);
                }
                double last = price - tax;
                // Send taxes
                GlobalMarketApi.sendTaxes(v.getPayType(), v.getEconomyType(), tax);
                // Determine if economy type exists
                if (BaseEconomy.PAY_TYPES.containsKey(v.getPayType())) {
                    DecimalFormat df = new DecimalFormat("#0.00");
                    BaseEconomy.getEconomyBridge(v.getPayType()).give(player, v.getEconomyType(), last);
                    player.sendMessage(I18n.getStrAndHeader("sale-sell")
                            .replace("%market%", marketData == null ? "" : marketData.getDisplayName())
                            .replace("%economy%", marketData == null ? "" : marketData.getEconomyName())
                            .replace("%money%", df.format(price))
                            .replace("%last%", df.format(last))
                            .replace("%buyer%", v.getBuyer()));
                }
            }
        });
    }

    @Override
    public long getDelay() {
        return GeneralOption.offlineTransactionInterval;
    }

    @Override
    public long getPeroid() {
        return GeneralOption.offlineTransactionInterval;
    }
}
