package com.upgrade.globalmarket.internal.cache.other;

import com.upgrade.globalmarket.internal.cache.sale.SaleCache;
import com.upgrade.globalmarket.internal.enums.LogType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * @author Blank038
 */
@Getter
@Builder
public class SaleLog {
    private final LogType logType;
    private UUID triggerPlayerUUID;
    private SaleCache saleCache;
    private String sourceMarket;
    private long triggerTime;
}
