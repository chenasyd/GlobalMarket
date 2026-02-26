package com.upgrade.globalmarket.internal.task;

import com.upgrade.globalmarket.api.GlobalMarketApi;
import com.upgrade.globalmarket.api.platform.wrapper.ITaskWrapper;
import com.upgrade.globalmarket.internal.plugin.GlobalMarket;

public abstract class AbstractTask implements ITask {
    private ITaskWrapper wrapper;

    @Override
    public ITaskWrapper getWrapper() {
        return wrapper;
    }

    @Override
    public void restart() {
        if (wrapper != null) {
            wrapper.cancel();
        }
        wrapper = GlobalMarketApi.getPlatformApi().runTaskTimerAsynchronously(
                GlobalMarket.getInstance(),
                this,
                getDelay(),
                getPeroid()
        );
    }
}
