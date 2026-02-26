package com.upgrade.globalmarket.internal.listen;

import com.upgrade.globalmarket.internal.plugin.GlobalMarket;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

/**
 * @author Blank038
 */
public abstract class AbstractListener implements Listener {

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, GlobalMarket.getInstance());
    }
}
