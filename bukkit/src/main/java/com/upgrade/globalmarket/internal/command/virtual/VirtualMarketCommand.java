package com.upgrade.globalmarket.internal.command.virtual;

import com.upgrade.globalmarket.api.GlobalMarketApi;
import com.upgrade.globalmarket.internal.cache.other.NotifyCache;
import com.upgrade.globalmarket.internal.plugin.GlobalMarket;
import com.upgrade.globalmarket.api.event.PlayerSaleEvent;
import com.upgrade.globalmarket.api.entity.MarketData;
import com.upgrade.globalmarket.internal.cache.sale.SaleCache;
import com.upgrade.globalmarket.internal.economy.BaseEconomy;
import com.upgrade.globalmarket.internal.enums.PayType;
import com.upgrade.globalmarket.api.handler.filter.FilterHandler;
import com.upgrade.globalmarket.api.handler.filter.impl.KeyFilterImpl;
import com.upgrade.globalmarket.internal.gui.impl.MarketGui;
import com.upgrade.globalmarket.internal.i18n.I18n;
import com.upgrade.globalmarket.internal.service.notify.NotifyCenter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * @author Blank038
 */
public class VirtualMarketCommand extends Command {
    private final MarketData marketData;

    public VirtualMarketCommand(MarketData marketData) {
        super(marketData.getShortCommand());
        this.marketData = marketData;
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            this.performSellCommand(player, strings);
        }
        return true;
    }

    private void performSellCommand(Player player, String[] args) {
        if (this.marketData.getPermission() != null && !this.marketData.getShortCommand().isEmpty()
                && !player.hasPermission(this.marketData.getPermission())) {
            player.sendMessage(I18n.getStrAndHeader("no-permission"));
            return;
        }
        if (args.length == 0) {
            new MarketGui(this.marketData.getMarketKey(), 1, null).openGui(player);
            return;
        }
        // Check first parameter is not "sell"
        if (!args[0].equalsIgnoreCase("sell")) {
            new MarketGui(this.marketData.getMarketKey(), 1, null).openGui(player);
            return;
        }
        if (args.length == 1) {
            player.sendMessage(I18n.getStrAndHeader("price-null"));
            return;
        }
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            player.sendMessage(I18n.getStrAndHeader("hand-air"));
            return;
        }
        ItemStack cloneItem = itemStack.clone();
        if (this.marketData.getDeniedFilter().check(cloneItem)) {
            player.sendMessage(I18n.getStrAndHeader("deny-item"));
            return;
        }
        int price;
        try {
            price = Integer.parseInt(args[1]);
        } catch (Exception e) {
            player.sendMessage(I18n.getStrAndHeader("wrong-number"));
            return;
        }
        if (price < this.marketData.getMin()) {
            player.sendMessage(I18n.getStrAndHeader("min-price")
                    .replace("%min%", String.valueOf(this.marketData.getMin())));
            return;
        }
        if (price > this.marketData.getMax()) {
            player.sendMessage(I18n.getStrAndHeader("max-price")
                    .replace("%max%", String.valueOf(this.marketData.getMax())));
            return;
        }
        String extraPrice = this.marketData.getExtraMap().entrySet().stream()
                .filter((s) -> new FilterHandler().addKeyFilter(new KeyFilterImpl(s.getKey())).check(cloneItem))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
        if (extraPrice != null && price < Integer.parseInt(extraPrice.split("-")[0])) {
            player.sendMessage(I18n.getStrAndHeader("min-price")
                    .replace("%min%", extraPrice.split("-")[0]));
            return;
        }
        if (extraPrice != null && price > Integer.parseInt(extraPrice.split("-")[1])) {
            player.sendMessage(I18n.getStrAndHeader("max-price")
                    .replace("%max%", extraPrice.split("-")[1]));
            return;
        }
        // Determine if sale count is not up
        int currentCount = GlobalMarket.getStorageHandler().getSaleCountByPlayer(player.getUniqueId(), this.marketData.getMarketKey());
        if (currentCount >= this.marketData.getPermsValueForPlayer(this.marketData.getLimitCountSection(), player, true)) {
            player.sendMessage(I18n.getStrAndHeader("maximum-sale"));
            return;
        }
        // Determine if balance is not enough
        double tax = price * this.marketData.getPermsValueForPlayer(this.marketData.getShoutTaxSection(), player, false);
        if (BaseEconomy.getEconomyBridge(this.marketData.getPaymentType()).balance(player, this.marketData.getEconomyType()) < tax) {
            player.sendMessage(I18n.getStrAndHeader("shout-tax")
                    .replace("%economy%", this.marketData.getEconomyName()));
            return;
        }
        if (tax > 0 && !BaseEconomy.getEconomyBridge(this.marketData.getPaymentType()).take(player, this.marketData.getEconomyType(), tax)) {
            player.sendMessage(I18n.getStrAndHeader("shout-tax")
                    .replace("%economy%", this.marketData.getEconomyName()));
            return;
        }
        // Initial SaleCache
        SaleCache saleItem = new SaleCache(UUID.randomUUID().toString(), this.marketData.getMarketKey(), player.getUniqueId().toString(),
                player.getName(), cloneItem, PayType.VAULT, this.marketData.getEconomyType(), price, System.currentTimeMillis());
        PlayerSaleEvent.Sell.Pre sellPreEvent = new PlayerSaleEvent.Sell.Pre(player, this.marketData, saleItem);
        Bukkit.getPluginManager().callEvent(sellPreEvent);
        if (sellPreEvent.isCancelled()) {
            return;
        }
        // Send taxes
        GlobalMarketApi.sendTaxes(this.marketData.getPaymentType(), this.marketData.getEconomyType(), tax);
        player.getInventory().setItemInMainHand(null);
        CompletableFuture.supplyAsync(() -> {
            // Add sale to storage handler
            return GlobalMarket.getStorageHandler().addSale(this.marketData.getMarketKey(), saleItem);
        }).exceptionally((e) -> {
            GlobalMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "Please contact the author at https://github.com/blank038/ServerMarket/issues");
            player.getInventory().addItem(cloneItem);
            player.sendMessage(I18n.getStrAndHeader("sale-failed"));
            return false;
        }).thenAccept((result) -> {
            Bukkit.getScheduler().runTask(GlobalMarket.getInstance(), () -> {
                if (result) {
                    // Call PlayerSaleEvent.Sell
                    PlayerSaleEvent.Sell.Post sellPostEvent = new PlayerSaleEvent.Sell.Post(player, this.marketData, saleItem);
                    Bukkit.getPluginManager().callEvent(sellPostEvent);

                    player.sendMessage(I18n.getStrAndHeader("sell"));
                    // Determine if broadcast is enabled
                    if (this.marketData.isSaleBroadcast()) {
                        String displayName = cloneItem.hasItemMeta() && cloneItem.getItemMeta().hasDisplayName() ?
                                cloneItem.getItemMeta().getDisplayName() : cloneItem.getType().name();
                        NotifyCache notify = new NotifyCache();
                        notify.message = I18n.getStrAndHeader("broadcast")
                                .replace("%item%", displayName)
                                .replace("%market_name%", this.marketData.getDisplayName())
                                .replace("%amount%", String.valueOf(cloneItem.getAmount()))
                                .replace("%player%", player.getName())
                                .replace("%price%", String.valueOf(price))
                                .replace("%economy%", this.marketData.getEconomyName());
                        notify.time = System.currentTimeMillis();
                        NotifyCenter.pushNotify(notify);
                    }
                } else {
                    player.getInventory().addItem(cloneItem);
                    player.sendMessage(I18n.getStrAndHeader("sale-denied"));
                }
            });
        });
    }
}
