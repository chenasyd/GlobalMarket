package com.upgrade.globalmarket.api.handler.sort.impl;

import com.upgrade.globalmarket.api.handler.sort.AbstractSortHandler;
import com.upgrade.globalmarket.internal.cache.sale.SaleCache;

/**
 * @author Blank038
 */
public class DefaultSortHandlerImpl extends AbstractSortHandler {

    public DefaultSortHandlerImpl() {
        super("default");
    }

    @Override
    public int compare(SaleCache o1, SaleCache o2) {
        // Sort by post time from oldest to newest
        long timeDiff = o2.getPostTime() - o1.getPostTime();
        if (timeDiff == 0) {
            return 0;
        }
        return timeDiff < 0 ? -1 : 1;
    }
}
