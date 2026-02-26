package com.upgrade.globalmarket.api.handler.filter.interfaces;

import com.upgrade.globalmarket.internal.cache.sale.SaleCache;
import org.bukkit.inventory.ItemStack;

public interface IFilter {

    boolean check(SaleCache saleItem);

    boolean check(ItemStack itemStack);
}
