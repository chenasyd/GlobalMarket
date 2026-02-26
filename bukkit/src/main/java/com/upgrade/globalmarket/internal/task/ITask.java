package com.upgrade.globalmarket.internal.task;

import com.upgrade.globalmarket.api.platform.wrapper.ITaskWrapper;

public interface ITask extends Runnable {

    ITaskWrapper getWrapper();

    long getDelay();

    long getPeroid();

    void restart();
}
