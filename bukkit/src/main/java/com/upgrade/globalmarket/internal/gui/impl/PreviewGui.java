package com.upgrade.globalmarket.internal.gui.impl;

import com.aystudio.core.bukkit.util.inventory.GuiModel;
import com.upgrade.globalmarket.api.entity.MarketData;
import com.upgrade.globalmarket.api.handler.filter.FilterHandler;
import com.upgrade.globalmarket.internal.gui.AbstractGui;
import com.upgrade.globalmarket.internal.plugin.GlobalMarket;
import com.upgrade.globalmarket.internal.i18n.I18n;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.NBTList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.block.ShulkerBox;

/**
 * Class
 *
 * @author Blank038
 */
public class PreviewGui extends AbstractGui {

    private final ItemStack shulkerItem;
    private final String marketKey;
    private final int page;
    private final FilterHandler filter;

    public PreviewGui(ItemStack shulkerItem, MarketData marketData, int page, FilterHandler filter) {
        this.shulkerItem = shulkerItem;
        this.marketKey = marketData.getMarketKey();
        this.page = page;
        this.filter = filter;
    }

    /**
     * Open preview GUI
     *
     * @param player target player
     */
    public void openGui(Player player) {
        // Create 3 rows x 9 cols preview GUI
        String title = I18n.getStrAndHeader("preview-title");
        // Get prefix length and remove
        String prefix = I18n.getOption("prefix");
        if (title.startsWith(prefix)) {
            title = title.substring(prefix.length());
        }
        title = title + shulkerItem.getType().name();
        GuiModel model = new GuiModel(title, 27);
        model.registerListener(GlobalMarket.getInstance());
        model.setCloseRemove(true);

        // ReadNBTinofList - Use BlockStateMeta method
        try {
            ItemStack[] contents = getShulkerContents(shulkerItem);
            GlobalMarket.getInstance().getLogger().info("Read " + contents.length + " items");
            int count = 0;
            for (int i = 0; i < Math.min(contents.length, 27); i++) {
                ItemStack item = contents[i];
                if (item != null && item.getType() != Material.AIR) {
                    model.setItem(i, item);
                    count++;
                    GlobalMarket.getInstance().getLogger().info("Slot " + i + ": " + item.getType().name());
                }
            }
            GlobalMarket.getInstance().getLogger().info("Displayed " + count + " items");
        } catch (Exception e) {
            GlobalMarket.getInstance().getLogger().severe("Failed to read shulker contents: " + e.getMessage());
            e.printStackTrace();
        }

        // Handle click event - read only mode, player cannot take items
        model.execute((e) -> {
            e.setCancelled(true);
            if (e.getClickedInventory() == e.getInventory()) {
                // Users can only view, cannot take items
                player.sendMessage(I18n.getStrAndHeader("preview-only-read"));
            }
        });

        //  Bukkit EventListenClosebackReturnMarket
        org.bukkit.event.Listener listener = new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
                if (event.getPlayer() == player) {
                    // Check if this is preview interface through title match
                    String inventoryTitle = event.getView().getTitle();
                    if (inventoryTitle.contains(shulkerItem.getType().name())) {
                        Bukkit.getScheduler().runTaskLater(GlobalMarket.getInstance(), () -> {
                            new MarketGui(marketKey, page, filter).openGui(player);
                        }, 1L);
                        // Unregister listener
                        org.bukkit.event.HandlerList.unregisterAll(this);
                    }
                }
            }
        };
        Bukkit.getPluginManager().registerEvents(listener, GlobalMarket.getInstance());

        // Open interface
        model.openInventory(player);
    }

    /**
     * Get item list inside shulker
     */
    private ItemStack[] getShulkerContents(ItemStack shulkerBox) {
        ItemStack[] contents = new ItemStack[27];
        try {
            if (shulkerBox.hasItemMeta() && shulkerBox.getItemMeta() instanceof BlockStateMeta) {
                BlockStateMeta bsm = (BlockStateMeta) shulkerBox.getItemMeta();
                if (bsm.getBlockState() instanceof ShulkerBox) {
                    ShulkerBox shulker = (ShulkerBox) bsm.getBlockState();
                    ItemStack[] shulkerContents = shulker.getInventory().getContents();
                    System.arraycopy(shulkerContents, 0, contents, 0, Math.min(shulkerContents.length, 27));
                    return contents;
                }
            }
            // If BlockStateMeta method failed, try NBT method
            return getShulkerContentsFromNBT(shulkerBox);
        } catch (Exception e) {
            GlobalMarket.getInstance().getLogger().warning("Using BlockStateMeta read failed, try NBT method: " + e.getMessage());
            return getShulkerContentsFromNBT(shulkerBox);
        }
    }

    /**
     * Read shulker contents from NBT
     */
    private ItemStack[] getShulkerContentsFromNBT(ItemStack shulkerBox) {
        ItemStack[] contents = new ItemStack[27];
        try {
            NBTItem nbtItem = new NBTItem(shulkerBox);
            if (nbtItem.hasTag("BlockEntityTag")) {
                NBTCompound blockEntityTag = nbtItem.getCompound("BlockEntityTag");
                if (blockEntityTag.hasTag("Items")) {
                    de.tr7zw.nbtapi.NBTCompoundList itemsList = blockEntityTag.getCompoundList("Items");
                    GlobalMarket.getInstance().getLogger().info("NBT Items ListSize: " + itemsList.size());
                    for (de.tr7zw.nbtapi.iface.ReadWriteNBT nbtCompound : itemsList) {
                        try {
                            int slot = nbtCompound.getByte("Slot");
                            if (slot >= 0 && slot < 27) {
                                ItemStack item = NBTItem.convertNBTtoItem((NBTCompound) nbtCompound);
                                if (item != null && item.getType() != Material.AIR) {
                                    contents[slot] = item;
                                }
                            }
                        } catch (Exception e) {
                            GlobalMarket.getInstance().getLogger().warning("Failed to read single item: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            GlobalMarket.getInstance().getLogger().warning("NBT read completely failed: " + e.getMessage());
            e.printStackTrace();
        }
        return contents;
    }
}
