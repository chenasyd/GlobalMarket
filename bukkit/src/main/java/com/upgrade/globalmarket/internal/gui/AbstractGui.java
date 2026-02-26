package com.upgrade.globalmarket.internal.gui;

import com.upgrade.globalmarket.api.GlobalMarketApi;
import com.upgrade.globalmarket.internal.plugin.GlobalMarket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Blank038
 */
public abstract class AbstractGui {
    protected static final Map<UUID, Long> COOLDOWN = new HashMap<>();

    static {
        GlobalMarketApi.getPlatformApi().runTaskTimerAsynchronously(GlobalMarket.getInstance(), () -> {
            synchronized (COOLDOWN) {
                COOLDOWN.entrySet().removeIf((entry) -> System.currentTimeMillis() > entry.getValue());
            }
        }, 60, 60);
    }

    public boolean isCooldown(UUID uuid) {
        if (System.currentTimeMillis() <= COOLDOWN.getOrDefault(uuid, 0L)) {
            return true;
        }
        COOLDOWN.put(uuid, System.currentTimeMillis() + GlobalMarket.getInstance().getConfig().getInt("cooldown.action"));
        return false;
    }
}
