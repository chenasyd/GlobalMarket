package com.upgrade.globalmarket.internal.platform.folia;

import com.upgrade.globalmarket.api.platform.IPlatformApi;
import com.upgrade.globalmarket.api.platform.wrapper.ITaskWrapper;
import com.upgrade.globalmarket.internal.platform.folia.wrapper.FoliaTaskWrapper;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

/**
 * @author Blank038
 */
public class FoliaPlatformApi implements IPlatformApi {

    @Override
    public ITaskWrapper runTask(JavaPlugin plugin, Runnable runnable) {
        ScheduledTask task = Bukkit.getServer().getGlobalRegionScheduler().run(plugin, (t) -> runnable.run());
        return new FoliaTaskWrapper(task);
    }

    @Override
    public ITaskWrapper runTaskAsynchronously(JavaPlugin plugin, Runnable runnable) {
        ScheduledTask task = Bukkit.getServer().getAsyncScheduler().runNow(plugin, (t) -> runnable.run());
        return new FoliaTaskWrapper(task);
    }

    @Override
    public ITaskWrapper runTaskTimerAsynchronously(JavaPlugin plugin, Runnable runnable, long delaySecond, long periodSecond) {
        ScheduledTask task = Bukkit.getServer().getAsyncScheduler().runAtFixedRate(plugin, (t) -> runnable.run(), delaySecond, delaySecond, TimeUnit.SECONDS);
        return new FoliaTaskWrapper(task);
    }
}
