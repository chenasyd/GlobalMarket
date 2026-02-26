package com.upgrade.globalmarket.internal.enums;

import com.upgrade.globalmarket.internal.economy.BaseEconomy;
import com.upgrade.globalmarket.internal.economy.impl.NyEconomyImpl;
import com.upgrade.globalmarket.internal.economy.impl.PlayerPointEconomyImpl;
import com.upgrade.globalmarket.internal.economy.impl.VaultEconomyImpl;
import lombok.Getter;

/**
 * Payment Type Enumeration
 * Payment type enumeration for economy plugins
 * @author Blank038
 * @date 2021/03/05
 */
@Getter
public enum PayType {
    /**
     * Vault Plugin
     */
    VAULT("Vault", VaultEconomyImpl.class),
    /**
     * PlayerPoints Plugin
     */
    PLAYER_POINTS("PlayerPoints", PlayerPointEconomyImpl.class),
    /**
     * NyEconomy Plugin
     */
    NY_ECONOMY("NyEconomy", NyEconomyImpl.class);

    private final String plugin;
    private final Class<? extends BaseEconomy> bridgeClass;

    PayType(String plugin, Class<? extends BaseEconomy> c) {
        this.plugin = plugin;
        this.bridgeClass = c;
    }
}
