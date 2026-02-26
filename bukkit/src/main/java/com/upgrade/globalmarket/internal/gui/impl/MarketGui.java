package com.upgrade.globalmarket.internal.gui.impl;

import com.aystudio.core.bukkit.util.common.CommonUtil;
import com.aystudio.core.bukkit.util.inventory.GuiModel;
import com.upgrade.globalmarket.internal.handler.CacheHandler;
import com.upgrade.globalmarket.internal.plugin.GlobalMarket;
import com.upgrade.globalmarket.internal.data.DataContainer;
import com.upgrade.globalmarket.internal.cache.sale.SaleCache;
import com.upgrade.globalmarket.api.entity.MarketData;
import com.upgrade.globalmarket.internal.enums.MarketStatus;
import com.upgrade.globalmarket.api.handler.filter.FilterHandler;
import com.upgrade.globalmarket.api.handler.filter.impl.TypeFilterImpl;
import com.upgrade.globalmarket.internal.gui.AbstractGui;
import com.upgrade.globalmarket.internal.i18n.I18n;
import com.upgrade.globalmarket.internal.util.ItemUtil;
import com.upgrade.globalmarket.internal.util.TextUtil;
import com.google.common.collect.Lists;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Blank038
 */
public class MarketGui extends AbstractGui {
    private final String sourceMarketKey;
    private FilterHandler filter;
    private int currentPage;
    private String currentType = "all", currentSort = "default";

    public MarketGui(String sourceMarketKey, int currentPage, FilterHandler filter) {
        this.sourceMarketKey = sourceMarketKey;
        this.currentPage = currentPage;
        this.filter = filter;
        if (this.filter == null) {
            this.filter = new FilterHandler();
        }
        if (this.filter.getTypeFilter() == null) {
            this.filter.setTypeFilter(new TypeFilterImpl(Lists.newArrayList(this.currentType)));
        } else {
            this.currentType = ((TypeFilterImpl) this.filter.getTypeFilter()).getTypes().get(0);
        }
    }

    /**
     * OpenMarketPanel
     *
     * @param player target player
     */
    public void openGui(Player player) {
        MarketData marketData = DataContainer.MARKET_DATA.get(this.sourceMarketKey);
        if (marketData == null || marketData.getMarketStatus() == MarketStatus.ERROR) {
            player.sendMessage(I18n.getStrAndHeader("market-error"));
            return;
        }
        if (marketData.getPermission() != null && !marketData.getPermission().isEmpty() && !player.hasPermission(marketData.getPermission())) {
            player.sendMessage(I18n.getStrAndHeader("no-permission"));
            return;
        }
        if (!GlobalMarket.getStorageHandler().getPlayerDataByCache(player.getUniqueId()).isPresent()) {
            return;
        }
        // ReadConfigureFile
        FileConfiguration data = YamlConfiguration.loadConfiguration(marketData.getSourceFile());
        GuiModel model = new GuiModel(data.getString("title"), data.getInt("size"));
        model.registerListener(GlobalMarket.getInstance());
        model.setCloseRemove(true);
        // Set
        this.initializeDisplayItem(model, data);
        // Get sale list
        Integer[] slots = CommonUtil.formatSlots(data.getString("sale-item-slots"));
        List<SaleCache> saleList = CacheHandler.querySales(this.sourceMarketKey)
                .values().stream()
                .filter((entry) -> (filter == null || filter.check(entry)))
                .sorted(DataContainer.SORT_HANDLER_MAP.get(this.currentSort))
                .collect(Collectors.toList());
        // Calculatedown
        int maxPage = saleList.size() / slots.length;
        maxPage += (saleList.size() % slots.length) == 0 ? 0 : 1;
        // Check if page exceeds max, reset to 1
        if (currentPage > maxPage) {
            currentPage = 1;
        }
        // Calculate start and end indexes
        int start = slots.length * (currentPage - 1), end = slots.length * currentPage;
        for (int i = start, index = 0; i < end; i++, index++) {
            if (index >= slots.length || i >= saleList.size()) {
                break;
            }
            SaleCache saleItem = saleList.get(i);
            if (saleItem == null || (filter != null && !filter.check(saleItem))) {
                --index;
                continue;
            }
            model.setItem(slots[index], this.getShowItem(marketData, saleItem, data));
        }
        final int lastPage = currentPage, finalMaxPage = maxPage;
        model.execute((e) -> {
            e.setCancelled(true);
            if (e.getClickedInventory() == e.getInventory()) {
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
                String key = nbtItem.getString("SaleUUID"), action = nbtItem.getString("MarketAction");
                if (key != null && !key.isEmpty()) {
                    DataContainer.MARKET_DATA.get(this.sourceMarketKey).action(clicker, key, e.getClick(), lastPage, filter);
                } else if (action != null && !action.isEmpty()) {
                    switch (action) {
                        case "up":
                            if (lastPage == 1) {
                                clicker.sendMessage(I18n.getStrAndHeader("no-previous-page"));
                            } else {
                                this.currentPage -= 1;
                                this.openGui(clicker);
                            }
                            break;
                        case "down":
                            if (lastPage >= finalMaxPage) {
                                clicker.sendMessage(I18n.getStrAndHeader("no-next-page"));
                            } else {
                                this.currentPage += 1;
                                this.openGui(clicker);
                            }
                            break;
                        case "type":
                            this.nextType(marketData, clicker);
                            break;
                        case "sort":
                            this.nextSort(clicker);
                            break;
                        case "store":
                            new StoreContainerGui(clicker, lastPage, this.sourceMarketKey, this.filter).open(1);
                            break;
                        case "refresh":
                            new MarketGui(this.sourceMarketKey, this.currentPage, this.filter).openGui(clicker);
                            break;
                        default:
                            if (action.contains(":")) {
                                String[] commands = action.split("\n");
                                for (String command : commands) {
                                    String[] split = command.split(":");
                                    if (split.length < 2) {
                                        continue;
                                    }
                                    if ("player".equalsIgnoreCase(split[0])) {
                                        Bukkit.getServer().dispatchCommand(clicker, split[1].replace("%player%", clicker.getName()));
                                    } else if ("console".equalsIgnoreCase(split[0])) {
                                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), split[1].replace("%player%", clicker.getName()));
                                    }
                                }
                            }
                            break;
                    }
                }
            }
        });
        // Open interface
        model.openInventory(player);
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
                // StartSetLore
                List<String> list = new ArrayList<>(section.getStringList("lore"));
                list.replaceAll((s) -> TextUtil.formatHexColor(s)
                        .replace("%saleType%", this.getCurrentTypeDisplayName())
                        .replace("%sortType%", this.getCurrentSortDisplayName()));
                // Special handling for changeType button - show all types with checkmark
                if ("type".equals(section.getString("action"))) {
                    List<String> typeLore = new ArrayList<>();
                    typeLore.add("&f选择分类:");
                    // First add "all" type
                    String allDisplayName = DataContainer.SALE_TYPE_DISPLAY_NAME.getOrDefault("all", "all");
                    if ("all".equals(this.currentType)) {
                        typeLore.add("&a✓ " + allDisplayName);
                    } else {
                        typeLore.add("&f  " + allDisplayName);
                    }
                    // Then add custom types
                    for (Map.Entry<String, List<String>> entry : DataContainer.SALE_TYPES.entrySet()) {
                        String typeKey = entry.getKey();
                        String displayName = DataContainer.SALE_TYPE_DISPLAY_NAME.getOrDefault(typeKey, typeKey);
                        if (typeKey.equals(this.currentType)) {
                            typeLore.add("&a✓ " + displayName);
                        } else {
                            typeLore.add("&f  " + displayName);
                        }
                    }
                    itemMeta.setLore(typeLore.stream().map(TextUtil::formatHexColor).collect(Collectors.toList()));
                } else if ("sort".equals(section.getString("action"))) {
                    List<String> sortLore = new ArrayList<>();
                    sortLore.add("&f选择排序:");
                    for (Map.Entry<String, String> entry : DataContainer.SORT_TYPE_DISPLAY_NAME.entrySet()) {
                        String sortKey = entry.getKey();
                        String displayName = entry.getValue();
                        if (sortKey.equals(this.currentSort)) {
                            sortLore.add("&a✓ " + displayName);
                        } else {
                            sortLore.add("&f  " + displayName);
                        }
                    }
                    itemMeta.setLore(sortLore.stream().map(TextUtil::formatHexColor).collect(Collectors.toList()));
                } else {
                    itemMeta.setLore(list.stream().map(TextUtil::formatHexColor).collect(Collectors.toList()));
                }
                itemStack.setItemMeta(itemMeta);
                // Start determine action
                if (section.contains("action")) {
                    NBTItem nbtItem = new NBTItem(itemStack);
                    nbtItem.setString("MarketAction", section.getString("action"));
                    itemStack = nbtItem.getItem();
                }
                for (int i : CommonUtil.formatSlots(section.getString("slot"))) {
                    model.setItem(i, itemStack);
                }
            }
        }
    }

    private String getCurrentTypeDisplayName() {
        return DataContainer.SALE_TYPE_DISPLAY_NAME.getOrDefault(this.currentType, this.currentType);
    }

    private String getCurrentSortDisplayName() {
        return DataContainer.SORT_TYPE_DISPLAY_NAME.getOrDefault(this.currentSort, this.currentSort);
    }

    private void nextType(MarketData marketData, Player clicker) {
        List<String> types = marketData.getSaleTypes();
        if (types.size() <= 1) {
            return;
        }
        int index = types.indexOf(currentType);
        this.currentType = (index == -1 || index == types.size() - 1) ? types.get(0) : types.get(index + 1);
        this.filter.setTypeFilter(new TypeFilterImpl(Lists.newArrayList(this.currentType)));
        this.openGui(clicker);
        clicker.sendMessage(I18n.getStrAndHeader("changeSaleType").replace("%type%", this.getCurrentTypeDisplayName()));
    }

    private void nextSort(Player clicker) {
        List<String> sorts = new ArrayList<>(DataContainer.SORT_HANDLER_MAP.keySet());
        if (sorts.size() <= 1) {
            return;
        }
        int index = sorts.indexOf(currentSort);
        this.currentSort = (index == -1 || index == sorts.size() - 1) ? sorts.get(0) : sorts.get(index + 1);
        this.openGui(clicker);
        clicker.sendMessage(I18n.getStrAndHeader("changeSortType").replace("%type%", this.getCurrentSortDisplayName()));
    }

    /**
     * ObtainMarket
     *
     * @param saleItem MarketItemMessage
     * @return 
     */
    private ItemStack getShowItem(MarketData marketData, SaleCache saleItem, FileConfiguration data) {
        ItemStack itemStack = saleItem.getSaleItem();
        ItemMeta itemMeta = itemStack.getItemMeta();
        String displayName = itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : itemStack.getType().name();
        if (data.contains("sale-name")) {
            String finalName = data.getString("sale-name").replace("%name%", displayName);
            itemMeta.setDisplayName(TextUtil.formatHexColor(finalName));
        }
        if (marketData.isShowSaleInfo()) {
            List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : Lists.newArrayList();
            // SetFormat
            Date date = new Date(saleItem.getPostTime());
            SimpleDateFormat sdf = new SimpleDateFormat(marketData.getDateFormat());
            // SetoutsideMessage
            for (String i : data.getStringList("sale-info")) {
                lore.add(TextUtil.formatHexColor(i)
                        .replace("%seller%", saleItem.getOwnerName())
                        .replace("%price%", String.format(marketData.getPriceFormat(), saleItem.getPrice()))
                        .replace("%time%", sdf.format(date))
                        .replace("%name%", displayName));
            }
            // Add preview hint for shulker box
            if (itemStack.getType().name().contains("SHULKER_BOX")) {
                lore.add(TextUtil.formatHexColor("&erightKey"));
            }
            itemMeta.setLore(lore);
        }
        itemStack.setItemMeta(itemMeta);
        // Add uuid to the sale nbt.
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setString("SaleUUID", saleItem.getSaleUUID());
        return nbtItem.getItem();
    }
}
