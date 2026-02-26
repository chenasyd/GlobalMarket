package com.upgrade.globalmarket.internal.plugin;

import com.aystudio.core.bukkit.plugin.AyPlugin;
import com.upgrade.globalmarket.api.GlobalMarketApi;
import com.upgrade.globalmarket.api.handler.sort.AbstractSortHandler;
import com.upgrade.globalmarket.internal.config.GeneralOption;
import com.upgrade.globalmarket.internal.data.convert.LegacyBackup;
import com.upgrade.globalmarket.dto.AbstractStorageHandler;
import com.upgrade.globalmarket.dto.IStorageHandler;
import com.upgrade.globalmarket.internal.economy.BaseEconomy;
import com.upgrade.globalmarket.internal.command.MainCommand;
import com.upgrade.globalmarket.internal.data.DataContainer;
import com.upgrade.globalmarket.internal.handler.TaskHandler;
import com.upgrade.globalmarket.internal.i18n.I18n;
import com.upgrade.globalmarket.internal.listen.impl.CoreListener;
import com.upgrade.globalmarket.internal.listen.impl.PlayerCommonListener;
import com.upgrade.globalmarket.internal.listen.impl.PlayerLatestListener;
import com.upgrade.globalmarket.internal.metrics.Metrics;
import com.upgrade.globalmarket.internal.platform.PlatformHandler;
import com.google.common.collect.Lists;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Global market plugin for Bukkit.
 *
 * @author Blank038
 */
@SuppressWarnings(value = {"unused"})
public class GlobalMarket extends AyPlugin {
    @Getter
    private static GlobalMarket instance;
    @Getter
    @Setter
    private static IStorageHandler storageHandler;


    @Override
    public void onEnable() {
        instance = this;
        // initialize platform
        PlatformHandler.initPlatform();
        // begin loading
        this.getConsoleLogger().setPrefix("&f[&eGlobalMarket&f] &8");
        this.loadConfig(true);
        // register command executor
        new MainCommand(this).register();
        // register listeners
        new CoreListener().register();
        new PlayerCommonListener().register();
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_13_R1)) {
            new PlayerLatestListener().register();
        }
        // register sort handler
        AbstractSortHandler.registerDefaults();
        // inject metrics
        new Metrics(this, 20031);
    }

    @Override
    public void onDisable() {
        if (storageHandler != null) {
            storageHandler.saveAll();
        }
    }

    public void loadConfig(boolean start) {
        this.getConsoleLogger().log(false, " ");
        this.getConsoleLogger().log(false, "   &3GlobalMarket &bv" + this.getDescription().getVersion());
        this.getConsoleLogger().log(false, " ");
        this.saveDefaultConfig();
        this.reloadConfig();
        GeneralOption.init();
        // Initialize I18n
        I18n.init(this.getConfig().getString("language", "zh_CN"));
        // Run legacy converter
        LegacyBackup.check();
        // Save the default files
        Lists.newArrayList("gui/store.yml").forEach((v) -> this.saveResource(v, v));
        if (start && this.isEnabled()) {
            // Initialize IStorageHandler
            AbstractStorageHandler.check();
            storageHandler.reload();
            // Initialize economy
            BaseEconomy.initEconomies();
        }
        // Initialize DataContainer
        DataContainer.loadData();
        // register service
        ConfigurationSection section = this.getConfig().getConfigurationSection("notify-option");
        String serviceType = section.getString("use", "self");
        GlobalMarketApi.createService(serviceType, section.getConfigurationSection("type." + serviceType));
        // Restart internal tasks
        TaskHandler.restartInternalTasks();

        this.getConsoleLogger().log(false, I18n.getProperties().getProperty("load-completed")
                .replace("%s", String.valueOf(DataContainer.MARKET_DATA.size())));
        this.getConsoleLogger().log(false, " ");
    }

    public static IStorageHandler getStorageHandler() {
        return storageHandler;
    }

    public static void setStorageHandler(IStorageHandler handler) {
        GlobalMarket.storageHandler = handler;
    }
}
