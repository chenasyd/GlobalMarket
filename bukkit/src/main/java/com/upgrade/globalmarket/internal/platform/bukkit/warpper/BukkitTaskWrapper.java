package com.upgrade.globalmarket.internal.platform.bukkit.warpper;

import com.upgrade.globalmarket.api.platform.wrapper.ITaskWrapper;
import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitTask;

/**
 * @author Blank038
 */
@AllArgsConstructor
public class BukkitTaskWrapper implements ITaskWrapper {

    public final BukkitTask bukkitTask;

    @Override
    public void cancel() {
        bukkitTask.cancel();
    }
}
