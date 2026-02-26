package com.upgrade.globalmarket.internal.task.impl;

import com.upgrade.globalmarket.internal.cache.sale.SaleCache;
import com.upgrade.globalmarket.internal.config.GeneralOption;
import com.upgrade.globalmarket.internal.data.DataContainer;
import com.upgrade.globalmarket.internal.handler.CacheHandler;
import com.upgrade.globalmarket.internal.plugin.GlobalMarket;
import com.upgrade.globalmarket.internal.task.AbstractTask;

import java.util.Map;

public class CacheUpdateTask extends AbstractTask {

    @Override
    public long getDelay() {
        return GeneralOption.cacheUpdateInterval;
    }

    @Override
    public long getPeroid() {
        return GeneralOption.cacheUpdateInterval;
    }

    @Override
    public void run() {
        DataContainer.MARKET_DATA.forEach((k, v) -> {
            Map<String, SaleCache> map = GlobalMarket.getStorageHandler().getSaleItemsByMarket(k);
            CacheHandler.uploadSales(k, map);
        });
    }
}
