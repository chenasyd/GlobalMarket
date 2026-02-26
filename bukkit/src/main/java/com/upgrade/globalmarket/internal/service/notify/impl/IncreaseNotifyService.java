package com.upgrade.globalmarket.internal.service.notify.impl;

import com.upgrade.globalmarket.api.GlobalMarketApi;
import com.upgrade.globalmarket.api.platform.wrapper.ITaskWrapper;
import com.upgrade.globalmarket.internal.plugin.GlobalMarket;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Blank038
 */
public abstract class IncreaseNotifyService extends AbstractNotifyService {
    private final List<Integer> pushedIndexes = new ArrayList<>();
    private ITaskWrapper wrapper;

    public boolean hasIndex(int index) {
        return this.pushedIndexes.contains(index);
    }

    public void addIndex(int index) {
        this.pushedIndexes.add(index);
    }
    
    public void update() {
    }
    
    public void runTask(int seconds) {
        if (wrapper != null) {
            wrapper.cancel();
        }
        wrapper = GlobalMarketApi.getPlatformApi().runTaskTimerAsynchronously(
                GlobalMarket.getInstance(),
                this::update,
                seconds,
                seconds
        );
    }
}
