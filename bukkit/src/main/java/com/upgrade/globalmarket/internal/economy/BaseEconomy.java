package com.upgrade.globalmarket.internal.economy;

import com.upgrade.globalmarket.internal.plugin.GlobalMarket;
import com.upgrade.globalmarket.internal.enums.PayType;
import com.upgrade.globalmarket.internal.i18n.I18n;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Blank038
 */
public abstract class BaseEconomy {
    public static final Map<PayType, BaseEconomy> PAY_TYPES = new HashMap<>();

    public BaseEconomy(PayType payType) {
        PAY_TYPES.put(payType, this);
        GlobalMarket.getInstance().getConsoleLogger().log(false,
                I18n.getProperties().getProperty("hook-economy").replace("%s", payType.getPlugin()));
    }

    /**
     * QueryBalance
     *
     * @param player target player
     * @param key    ?     * @return Balance
     */
    public abstract double balance(OfflinePlayer player, String key);

    /**
     * 
     *
     * @param player target player
     * @param key    Class
     * @param amount Quantity
     */
    public abstract void give(OfflinePlayer player, String key, double amount);

    /**
     * Deduct money from player
     *
     * @param player target player
     * @param key    Economy currency type
     * @param amount Quantity to deduct
     * @return true if successful, false otherwise
     */
    public abstract boolean take(OfflinePlayer player, String key, double amount);

    public static <T extends BaseEconomy> T register(Class<T> c) {
        try {
            return c.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            GlobalMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "cannot register economy: " + c.getName());
        }
        return null;
    }

    /**
     * Initialize
     */
    public static void initEconomies() {
        if (PAY_TYPES.isEmpty()) {
            for (PayType type : PayType.values()) {
                if (Bukkit.getPluginManager().getPlugin(type.getPlugin()) != null) {
                    BaseEconomy.register(type.getBridgeClass());
                }
            }
        }
    }

    public static BaseEconomy getEconomyBridge(PayType payType) {
        return BaseEconomy.PAY_TYPES.getOrDefault(payType, null);
    }
}
