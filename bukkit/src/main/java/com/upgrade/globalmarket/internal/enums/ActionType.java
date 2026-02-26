package com.upgrade.globalmarket.internal.enums;

import com.upgrade.globalmarket.api.GlobalMarketApi;
import com.upgrade.globalmarket.api.entity.MarketData;
import com.upgrade.globalmarket.api.event.PlayerSaleEvent;
import com.upgrade.globalmarket.api.handler.filter.FilterHandler;
import com.upgrade.globalmarket.internal.cache.sale.SaleCache;
import com.upgrade.globalmarket.internal.config.GeneralOption;
import com.upgrade.globalmarket.internal.economy.BaseEconomy;
import com.upgrade.globalmarket.internal.gui.impl.ConfirmPurchaseGui;
import com.upgrade.globalmarket.internal.gui.impl.MarketGui;
import com.upgrade.globalmarket.internal.gui.impl.PreviewGui;
import com.upgrade.globalmarket.internal.handler.CacheHandler;
import com.upgrade.globalmarket.internal.i18n.I18n;
import com.upgrade.globalmarket.internal.plugin.GlobalMarket;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.Optional;
import java.util.UUID;

public enum ActionType {
    PURCHASE((marketData, buyer, uuid, saleCache, page, filter) -> {
        if (saleCache.getOwnerUUID().equals(buyer.getUniqueId().toString())) {
            buyer.sendMessage(I18n.getStrAndHeader("is-owner"));
            return;
        }
        if (saleCache.getPrice() == 0) {
            buyer.sendMessage(I18n.getStrAndHeader("error-sale"));
            return;
        }
        if (BaseEconomy.getEconomyBridge(marketData.getPaymentType()).balance(buyer, marketData.getEconomyType()) < saleCache.getPrice()) {
            buyer.sendMessage(I18n.getStrAndHeader("lack-money").replace("%economy%", marketData.getEconomyName()));
            return;
        }
        Optional<SaleCache> optional = GlobalMarket.getStorageHandler().removeSaleItem(marketData.getSourceId(), uuid);
        if (optional.isPresent()) {
            SaleCache saleItem = optional.get();
            BaseEconomy.getEconomyBridge(marketData.getPaymentType()).take(buyer, marketData.getEconomyType(), saleItem.getPrice());
            Player seller = Bukkit.getPlayer(UUID.fromString(saleItem.getOwnerUUID()));
            if (seller != null && seller.isOnline()) {
                double tax = saleItem.getPrice() * marketData.getPermsValueForPlayer(marketData.getTaxSection(), seller, false);
                double last = saleItem.getPrice() - tax;
                DecimalFormat df = new DecimalFormat("#0.00");
                BaseEconomy.getEconomyBridge(marketData.getPaymentType()).give(seller, marketData.getEconomyType(), last);
                seller.sendMessage(I18n.getStrAndHeader("sale-sell")
                        .replace("%market%", marketData == null ? "" : marketData.getDisplayName())
                        .replace("%economy%", marketData.getEconomyName())
                        .replace("%money%", df.format(saleItem.getPrice()))
                        .replace("%last%", df.format(last))
                        .replaceAll("%buyer%", buyer.getName()));
                // send taxes
                GlobalMarketApi.sendTaxes(marketData.getPaymentType(), marketData.getEconomyName(), tax);
            } else {
                GlobalMarketApi.addOfflineTransaction(saleItem.getOwnerUUID(), buyer.getName(), marketData.getPaymentType(),
                        marketData.getEconomyType(), saleItem.getPrice(), marketData.getMarketKey());
            }
            // remove cache
            CacheHandler.removeSaleCache(marketData.getSourceId(), saleItem.getSaleUUID());
            // give sale item to buyer
            GlobalMarket.getStorageHandler().addItemToStore(buyer.getUniqueId(), saleItem, "buy");
            // call PlayerSaleEvent.Buy
            PlayerSaleEvent.Buy event = new PlayerSaleEvent.Buy(buyer, marketData, saleItem);
            Bukkit.getPluginManager().callEvent(event);
            // send message to buyer
            buyer.sendMessage(I18n.getStrAndHeader("buy-item"));
            new MarketGui(marketData.getMarketKey(), page, filter).openGui(buyer);
        } else {
            buyer.sendMessage(I18n.getStrAndHeader("error-sale"));
        }
    }),
    UNSALE((marketData, buyer, uuid, saleCache, page, filter) -> {
        if (saleCache.getOwnerUUID().equals(buyer.getUniqueId().toString())) {
            GlobalMarket.getStorageHandler().removeSaleItem(marketData.getSourceId(), uuid)
                    .ifPresent((sale) -> {
                        // remove cache
                        CacheHandler.removeSaleCache(marketData.getSourceId(), uuid);
                        // execute logic
                        GlobalMarket.getStorageHandler().addItemToStore(buyer.getUniqueId(), sale.getSaleItem(), "unsale");
                        buyer.sendMessage(I18n.getStrAndHeader("unsale"));
                        new MarketGui(marketData.getMarketKey(), page, filter).openGui(buyer);
                    });
        } else if (buyer.isOp() || buyer.hasPermission("servermarket.force-unsale")) {
            GlobalMarket.getStorageHandler().removeSaleItem(marketData.getSourceId(), uuid)
                    .ifPresent((sale) -> {
                        // remove cache
                        CacheHandler.removeSaleCache(marketData.getSourceId(), uuid);
                        // execute logic
                        if (!GeneralOption.restitution) {
                            buyer.getInventory().addItem(saleCache.getSaleItem());
                            buyer.sendMessage(I18n.getStrAndHeader("force-unsale"));
                            new MarketGui(marketData.getMarketKey(), page, filter).openGui(buyer);
                        } else {
                            UUID ownerUUID = UUID.fromString(sale.getOwnerUUID());
                            Player target = Bukkit.getPlayer(ownerUUID);
                            GlobalMarket.getStorageHandler().addItemToStore(ownerUUID, sale.getSaleItem(), "force-unsale");
                            if (target != null && target.isOnline()) {
                                target.sendMessage(I18n.getStrAndHeader("force-unsale-target"));
                            }
                            buyer.sendMessage(I18n.getStrAndHeader("force-unsale"));
                            new MarketGui(marketData.getMarketKey(), page, filter).openGui(buyer);
                        }
                    });
        } else {
            buyer.sendMessage(I18n.getStrAndHeader("not-owner"));
        }
    }),
    CONFIRM_PURCHASE((marketData, buyer, uuid, saleCache, page, filter) -> {
        if (saleCache.getOwnerUUID().equals(buyer.getUniqueId().toString())) {
            buyer.sendMessage(I18n.getStrAndHeader("is-owner"));
            return;
        }
        new ConfirmPurchaseGui().open(marketData, buyer, uuid, saleCache, page, filter);
    }),
    PREVIEW((marketData, buyer, uuid, saleCache, page, filter) -> {
        // Get item stack to preview
        ItemStack itemStack = saleCache.getSaleItem();
        if (itemStack.getType().name().contains("SHULKER_BOX")) {
            // Open preview GUI
            new PreviewGui(itemStack, marketData, page, filter).openGui(buyer);
        } else {
            // If not shulker box, execute default purchase action
            PURCHASE.run(marketData, buyer, uuid, saleCache, page, filter);
        }
    });

    private final ActionConsumer consumer;

    ActionType(ActionConsumer consumer) {
        this.consumer = consumer;
    }

    public void run(MarketData marketData, Player player, String uuid, SaleCache saleCache, int page, FilterHandler filter) {
        consumer.run(marketData, player, uuid, saleCache, page, filter);
    }

    @FunctionalInterface
    interface ActionConsumer {

        void run(MarketData marketData, Player player, String uuid, SaleCache saleCache, int page, FilterHandler filter);
    }
}
