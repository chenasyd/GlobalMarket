package com.upgrade.globalmarket.dto;

import com.upgrade.globalmarket.internal.plugin.GlobalMarket;
import com.upgrade.globalmarket.api.event.InitializeStorageHandlerEvent;
import com.upgrade.globalmarket.internal.cache.player.PlayerCache;
import com.upgrade.globalmarket.dto.impl.MysqlStorageHandlerImpl;
import com.upgrade.globalmarket.dto.impl.YamlStorageHandlerImpl;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Blank038
 */
public abstract class AbstractStorageHandler implements IStorageHandler {
    protected static final Map<UUID, PlayerCache> PLAYER_DATA_MAP = new HashMap<>();

    protected final GlobalMarket pluign = GlobalMarket.getInstance();

    @Override
    public void load(String market) {
    }

    @Override
    public void saveAllPlayerData() {
        PLAYER_DATA_MAP.entrySet().stream().forEach((entry) -> this.savePlayerData(entry.getValue(), false));
    }

    @Override
    public void setLock(UUID uuid, boolean locked) {
    }

    @Override
    public boolean isLocked(UUID uuid) {
        return false;
    }

    public void removePlyerData(UUID uuid) {
        PLAYER_DATA_MAP.entrySet().removeIf(entry -> entry.getKey().equals(uuid));
    }

    public static void check() {
        if (GlobalMarket.getStorageHandler() == null) {
            String storageType = GlobalMarket.getInstance().getConfig().getString("data-option.type").toLowerCase();
            IStorageHandler storageHandler = createStorageHandler(storageType);
            setStorageHandler(storageHandler);
        }
    }

    public static void setStorageHandler(IStorageHandler storageHandler) {
        Objects.requireNonNull(storageHandler, "The storageHandler must be not null");
        InitializeStorageHandlerEvent event = new InitializeStorageHandlerEvent(storageHandler);
        Bukkit.getPluginManager().callEvent(event);
        // Set storage handler
        GlobalMarket.setStorageHandler(event.getStorageHandler());
        GlobalMarket.getStorageHandler().initialize();
    }

    public static IStorageHandler createStorageHandler(String type) {
        switch (type.toLowerCase()) {
            case "mysql":
                return new MysqlStorageHandlerImpl();
            case "yaml":
                return new YamlStorageHandlerImpl();
            default:
                return null;
        }
    }
}
