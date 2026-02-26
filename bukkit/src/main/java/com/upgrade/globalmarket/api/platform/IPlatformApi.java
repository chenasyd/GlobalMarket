package com.upgrade.globalmarket.api.platform;

import com.upgrade.globalmarket.api.platform.wrapper.ITaskWrapper;
import org.bukkit.plugin.java.JavaPlugin;

public interface IPlatformApi {

    ITaskWrapper runTask(JavaPlugin plugin, Runnable runnable);

    ITaskWrapper runTaskAsynchronously(JavaPlugin plugin, Runnable runnable);

    ITaskWrapper runTaskTimerAsynchronously(JavaPlugin plugin, Runnable runnable, long delaySecond, long periodSecond);
}
