package com.upgrade.globalmarket.internal.platform;

import com.upgrade.globalmarket.api.GlobalMarketApi;
import com.upgrade.globalmarket.api.platform.IPlatformApi;
import com.upgrade.globalmarket.internal.platform.bukkit.BukkitPlatformApi;
import com.upgrade.globalmarket.internal.plugin.GlobalMarket;
import com.upgrade.globalmarket.internal.util.CoreUtil;

import java.util.logging.Level;

/**
 * @author Blank038
 */
public class PlatformHandler {

    public static void initPlatform() {
        if (CoreUtil.isFolia()) {
            try {
                Class<? extends IPlatformApi> classes = (Class<? extends IPlatformApi>) Class.forName("com.upgrade.globalmarket.internal.platform.folia.FoliaPlatformApi");
                setPlatform(classes.newInstance());
                return;
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                GlobalMarket.getInstance().getLogger().log(Level.WARNING, "Folia platform API not available, falling back to Bukkit platform");
            }
        }
        // Default to Bukkit platform
        setPlatform(new BukkitPlatformApi());
    }

    public static void setPlatform(IPlatformApi platform) {
        GlobalMarketApi.setPlatformApi(platform);
    }
}
