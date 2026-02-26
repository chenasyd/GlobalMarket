package com.upgrade.globalmarket.internal.gui.impl;

import com.aystudio.core.bukkit.util.common.CommonUtil;
import com.aystudio.core.bukkit.util.inventory.GuiModel;
import com.upgrade.globalmarket.api.GlobalMarketApi;
import com.upgrade.globalmarket.internal.plugin.GlobalMarket;
import com.upgrade.globalmarket.internal.cache.player.PlayerCache;
import com.upgrade.globalmarket.api.handler.filter.FilterHandler;
import com.upgrade.globalmarket.internal.gui.AbstractGui;
import com.upgrade.globalmarket.internal.i18n.I18n;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Blank038
 */
public class StoreContainerGui extends AbstractGui {
    private final Player target;
    private final int marketPage;
    private final String oldMarket;
    private final FilterHandler filterHandler;

    public StoreContainerGui(Player player, int marketPage, String oldMarket, FilterHandler filterHandler) {
        this.target = player;
        this.marketPage = marketPage;
        this.oldMarket = oldMarket;
        this.filterHandler = filterHandler;
    }

    public void open(int currentPage) {
        Optional<PlayerCache> playerData = GlobalMarket.getStorageHandler().getPlayerDataByCache(target.getUniqueId());
        if (playerData.isPresent()) {
            // File
            File file = new File(GlobalMarket.getInstance().getDataFolder(), "gui/store.yml");
            // ReadConfigureFile
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            GuiModel guiModel = new GuiModel(data.getString("title"), data.getInt("size"));
            // Set listener
            guiModel.registerListener(GlobalMarket.getInstance());
            guiModel.setCloseRemove(true);
            // Check if contains items section
            if (data.contains("items")) {
                for (String key : data.getConfigurationSection("items").getKeys(false)) {
                    ConfigurationSection section = data.getConfigurationSection("items." + key);
                    ItemStack itemStack = ItemUtil.generateItem(section.getString("type"),
                            section.getInt("amount"),
                            (short) section.getInt("data"),
                            section.getInt("customModel", -1));
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName(TextUtil.formatHexColor(section.getString("name")));
                    // Set lore
                    List<String> list = new ArrayList<>(section.getStringList("lore"));
                    list.replaceAll(TextUtil::formatHexColor);
                    itemMeta.setLore(list);
                    itemStack.setItemMeta(itemMeta);
                    // Check if has action
                    if (section.contains("action")) {
                        NBTItem nbtItem = new NBTItem(itemStack);
                        nbtItem.setString("action", section.getString("action"));
                        itemStack = nbtItem.getItem();
                    }
                    // Set item to slots
                    for (int i : CommonUtil.formatSlots(section.getString("slot"))) {
                        guiModel.setItem(i, itemStack);
                    }
                }
            }
            // Get slot array for store items
            Integer[] slots = CommonUtil.formatSlots(data.getString("store-item-slots"));
            Map<String, ItemStack> storeItems = playerData.get().getStoreItems();
            String[] keys = storeItems.keySet().toArray(new String[0]);
            // Calculate range
            int start = slots.length * (currentPage - 1), end = slots.length * currentPage;
            for (int i = start, index = 0; i < end; i++, index++) {
                if (index >= slots.length || i >= keys.length) {
                    break;
                }
                NBTItem nbtItem = new NBTItem(storeItems.get(keys[i]));
                nbtItem.setString("StoreID", keys[i]);
                guiModel.setItem(slots[index], nbtItem.getItem());
            }
            guiModel.execute((e) -> {
                e.setCancelled(true);
                if (e.getClickedInventory() == e.getInventory()) {
                    // Get clicker
                    Player clicker = (Player) e.getWhoClicked();
                    // Get clicked item
                    ItemStack itemStack = e.getCurrentItem();
                    if (itemStack == null || itemStack.getType() == Material.AIR) {
                        return;
                    }
                    if (this.isCooldown(clicker.getUniqueId())) {
                        clicker.sendMessage(I18n.getStrAndHeader("cooldown"));
                        return;
                    }
                    NBTItem nbtItem = new NBTItem(itemStack);
                    String storeId = nbtItem.getString("StoreID"), action = nbtItem.getString("action");
                    if ("market".equalsIgnoreCase(action)) {
                        GlobalMarketApi.openMarket(clicker, this.oldMarket, this.marketPage, this.filterHandler);
                    } else if (storeId != null && !storeId.isEmpty()) {
                        this.getItem(clicker, storeId, currentPage);
                    }
                }
            });
            guiModel.openInventory(target);
        }
    }

    public void getItem(Player player, String uuid, int currentPage) {
        GlobalMarket.getStorageHandler().getPlayerDataByCache(player.getUniqueId()).ifPresent((data) -> {
            if (data.hasStoreItem(uuid)) {
                if (player.getInventory().firstEmpty() == -1) {
                    player.sendMessage(I18n.getStrAndHeader("inventory-full"));
                    return;
                }
                // Remove item from store
                ItemStack itemStack = GlobalMarket.getStorageHandler().removeStoreItem(player.getUniqueId(), uuid);
                if (itemStack != null) {
                    String displayMmae = itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() ?
                            itemStack.getItemMeta().getDisplayName() : itemStack.getType().name();
                    player.getInventory().addItem(itemStack);
                    player.sendMessage(I18n.getStrAndHeader("get-store-item")
                            .replace("%item%", displayMmae)
                            .replace("%amount%", String.valueOf(itemStack.getAmount())));
                    // Refresh
                    this.open(currentPage);
                } else {
                    player.sendMessage(I18n.getStrAndHeader("error-store"));
                }
            } else {
                player.sendMessage(I18n.getStrAndHeader("error-store"));
            }
        });
    }
}
