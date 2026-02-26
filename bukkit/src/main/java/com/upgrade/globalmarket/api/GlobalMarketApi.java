package com.upgrade.globalmarket.api;

import com.upgrade.globalmarket.api.platform.IPlatformApi;
import com.upgrade.globalmarket.internal.economy.BaseEconomy;
import com.upgrade.globalmarket.internal.plugin.GlobalMarket;
import com.upgrade.globalmarket.internal.data.DataContainer;
import com.upgrade.globalmarket.api.entity.MarketData;
import com.upgrade.globalmarket.internal.cache.other.OfflineTransactionData;
import com.upgrade.globalmarket.internal.enums.PayType;
import com.upgrade.globalmarket.api.handler.filter.FilterHandler;
import com.upgrade.globalmarket.internal.gui.impl.MarketGui;
import com.upgrade.globalmarket.internal.service.notify.INotifyService;
import com.upgrade.globalmarket.internal.service.notify.NotifyCenter;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Global Market API
 *
 * @author Blank038
 * @date 2021/03/05
 */
public class GlobalMarketApi {
    @Getter
    @Setter
    private static IPlatformApi platformApi;

    public static List<String> getMarketList() {
        return new ArrayList<>(DataContainer.MARKET_DATA.keySet());
    }

    public static MarketData getMarketData(String marketKey) {
        return DataContainer.MARKET_DATA.getOrDefault(marketKey, null);
    }

    public static MarketData fuzzySearchMarketData(String key) {
        for (Map.Entry<String, MarketData> entry : DataContainer.MARKET_DATA.entrySet()) {
            if (entry.getKey().equals(key) || entry.getValue().getDisplayName().contains(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * OpenMarket, IfMarket?null OpenDefaultMarket
     *
     * @param player target player
     * @param key    Market
     */
    public static void openMarket(Player player, String key, int page, FilterHandler filter) {
        MarketData marketData = DataContainer.MARKET_DATA.containsKey(key) ? DataContainer.MARKET_DATA.get(key)
                : DataContainer.MARKET_DATA.get(GlobalMarket.getInstance().getConfig().getString("default-market"));
        if (marketData == null) {
            return;
        }
        if (player.hasPermission(marketData.getPermission())) {
            new MarketGui(marketData.getMarketKey(), page, filter).openGui(player);
        }
    }

    public static void addOfflineTransaction(String uuid, String buyer, PayType payType, String ectType, double moeny, String sourceMarket) {
        ConfigurationSection section = new YamlConfiguration();
        section.set("amount", moeny);
        section.set("pay-type", payType.name());
        section.set("owner-uuid", uuid);
        section.set("eco-type", ectType);
        section.set("source-market", sourceMarket);
        section.set("buyer", buyer);
        // Store data
        OfflineTransactionData resultData = new OfflineTransactionData(section);
        GlobalMarket.getStorageHandler().addOfflineTransaction(resultData);
    }

    public static void sendTaxes(PayType payType, String subData, double tax) {
        if (tax <= 0) {
            return;
        }
        String taxAccount = GlobalMarket.getInstance().getConfig().getString("tax-account", "");
        if (taxAccount.isEmpty() || taxAccount.equalsIgnoreCase("UUID")) {
            return;
        }
        try {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(taxAccount));
            if (offlinePlayer == null) {
                return;
            }
            BaseEconomy.getEconomyBridge(payType).give(offlinePlayer, subData, tax);
        } catch (Exception e) {
            GlobalMarket.getInstance().getLogger().log(Level.WARNING, "Fail to send the tax payment to " + taxAccount);
        }
    }

    public static void registerService(String source, Class<? extends INotifyService> classZ) {
        NotifyCenter.register(source, classZ);
    }

    public static INotifyService createService(String source, ConfigurationSection config) {
        if (!NotifyCenter.isRegister(source)) {
            return null;
        }
        try {
            INotifyService service = NotifyCenter.findNotifyClass(source).newInstance();
            return NotifyCenter.create(source, service, config);
        } catch (Exception e) {
            return null;
        }
    }
}
