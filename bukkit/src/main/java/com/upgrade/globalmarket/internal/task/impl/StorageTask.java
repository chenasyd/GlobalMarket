package com.upgrade.globalmarket.internal.task.impl;

import com.upgrade.globalmarket.internal.plugin.GlobalMarket;
import com.upgrade.globalmarket.internal.task.AbstractTask;

public class StorageTask extends AbstractTask {

    @Override
    public long getDelay() {
        return 60;
    }

    @Override
    public long getPeroid() {
        return 60;
    }

    @Override
    public void run() {
        GlobalMarket.getStorageHandler().saveAll();
    }
}
