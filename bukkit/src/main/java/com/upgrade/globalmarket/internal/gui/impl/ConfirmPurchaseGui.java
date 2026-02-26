package com.upgrade.globalmarket.internal.gui.impl;

import com.aystudio.core.bukkit.util.common.CommonUtil;
import com.aystudio.core.bukkit.util.inventory.GuiModel;
import com.upgrade.globalmarket.api.entity.MarketData;
import com.upgrade.globalmarket.api.handler.filter.FilterHandler;
import com.upgrade.globalmarket.internal.cache.sale.SaleCache;
import com.upgrade.globalmarket.internal.data.DataContainer;
import com.upgrade.globalmarket.internal.enums.ActionType;
import com.upgrade.globalmarket.internal.gui.AbstractGui;
import com.upgrade.globalmarket.internal.i18n.I18n;
import com.upgrade.globalmarket.internal.plugin.GlobalMarket;
import com.upgrade.globalmarket.internal.util.ItemUtil;
import com.upgrade.globalmarket.internal.util.TextUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Blank038
 */
public class ConfirmPurchaseGui extends AbstractGui {

    public void open(MarketData marketData, Player player, String uuid, SaleCache saleCache, int page, FilterHandler filter) {
        String key = DataContainer.isLegacy() ? "gui/confirm_purchase_legacy.yml" : "gui/confirm_purchase.yml";
        GlobalMarket.getInstance().saveResource(key, "gui/confirm_purchase.yml", false, (file) -> {
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            GuiModel model = new GuiModel(data.getString("title"), data.getInt("size"));

            this.initializeDisplayItem(model, data);
            model.setItem(data.getInt("item-slot"), saleCache.getSaleItem().clone());

            model.registerListener(GlobalMarket.getInstance());
            model.execute((e) -> {
                e.setCancelled(true);
                if (e.getClickedInventory() != e.getInventory()) {
                    return;
                }
                ItemStack itemStack = e.getCurrentItem();
                if (itemStack == null || itemStack.getType() == Material.AIR) {
                    return;
                }
                Player clicker = (Player) e.getWhoClicked();
                if (this.isCooldown(clicker.getUniqueId())) {
                    clicker.sendMessage(I18n.getStrAndHeader("cooldown"));
                    return;
                }
                NBTItem nbtItem = new NBTItem(itemStack);
                if (!nbtItem.hasTag("ConfirmAction")) {
                    return;
                }
                switch (nbtItem.getString("ConfirmAction")) {
                    case "confirm":
                        ActionType.PURCHASE.run(marketData, clicker, uuid, saleCache, page, filter);
                        break;
                    case "cancel":
                        new MarketGui(marketData.getMarketKey(), page, filter).openGui(clicker);
                        break;
                    default:
                        break;
                }
            });
            model.openInventory(player);
        });
    }

    private void initializeDisplayItem(GuiModel model, FileConfiguration data) {
        if (data.contains("items")) {
            for (String key : data.getConfigurationSection("items").getKeys(false)) {
                ConfigurationSection section = data.getConfigurationSection("items." + key);
                ItemStack itemStack = ItemUtil.generateItem(section.getString("type"),
                        section.getInt("amount"),
                        (short) section.getInt("data"),
                        section.getInt("customModel", -1));
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(TextUtil.formatHexColor(section.getString("name")));
                List<String> list = new ArrayList<>(section.getStringList("lore"));
                list.replaceAll(TextUtil::formatHexColor);
                itemMeta.setLore(list);
                itemStack.setItemMeta(itemMeta);
                if (section.contains("action")) {
                    NBTItem nbtItem = new NBTItem(itemStack);
                    nbtItem.setString("ConfirmAction", section.getString("action"));
                    itemStack = nbtItem.getItem();
                }
                for (int i : CommonUtil.formatSlots(section.getString("slot"))) {
                    model.setItem(i, itemStack);
                }
            }
        }
    }
}
