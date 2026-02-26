package com.upgrade.globalmarket.internal.provider;

import com.upgrade.globalmarket.api.entity.MarketData;
import com.upgrade.globalmarket.api.handler.filter.FilterHandler;
import com.upgrade.globalmarket.internal.cache.sale.SaleCache;
import com.upgrade.globalmarket.internal.data.DataContainer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

/**
 * @author Blank038
 */
public class ActionProvider {

    public static void runAction(MarketData marketData, Player player, String uuid, SaleCache saleCache, ClickType clickType, int page, FilterHandler filter) {
        if (!DataContainer.ACTION_TYPE_MAP.containsKey(clickType)) {
            return;
        }
        DataContainer.ACTION_TYPE_MAP.get(clickType).run(marketData, player, uuid, saleCache, page, filter);
    }
}
