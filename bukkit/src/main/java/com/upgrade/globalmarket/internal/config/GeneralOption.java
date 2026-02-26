package com.upgrade.globalmarket.internal.config;

import com.upgrade.globalmarket.internal.plugin.GlobalMarket;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author Blank038
 */
public class GeneralOption {
    public static boolean restitution;
    public static int offlineTransactionInterval;
    public static int cacheUpdateInterval;

    public static void init() {
        FileConfiguration config = GlobalMarket.getInstance().getConfig();
        restitution = config.getBoolean("settings.restitution");
        offlineTransactionInterval = config.getInt("settings.offline-transaction-interval");
        cacheUpdateInterval = config.getInt("settings.cache-update-interval");
    }
}
