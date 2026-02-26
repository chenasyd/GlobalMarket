package com.upgrade.globalmarket.internal.cache.player;

import com.upgrade.globalmarket.api.event.PlayerStoreItemAddEvent;
import com.upgrade.globalmarket.internal.cache.sale.SaleCache;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Blank038
 */
@Getter
public class PlayerCache {
    private final Map<String, ItemStack> storeItems = new HashMap<>();
    private final UUID ownerUniqueId;
    @Setter
    private boolean newData;

    public PlayerCache(UUID uuid, FileConfiguration data) {
        this.ownerUniqueId = uuid;
        // Read
        if (data.contains("items")) {
            for (String key : data.getConfigurationSection("items").getKeys(false)) {
                storeItems.put(key, data.getItemStack("items." + key));
            }
        }
    }

    public PlayerCache(UUID uuid, FileConfiguration data, boolean newData) {
        this(uuid, data);
        this.newData = newData;
    }

    /**
     * Check if store has item with given uuid
     *
     * @param uuid
     * @return true if contains, false otherwise
     */
    public boolean hasStoreItem(String uuid) {
        return storeItems.containsKey(uuid);
    }

    /**
     * Remove item from store
     *
     * @param uuid
     * @return Removed item
     */
    public ItemStack removeStoreItem(String uuid) {
        return storeItems.remove(uuid);
    }

    public Map<String, ItemStack> getStoreItems() {
        return new HashMap<>(storeItems);
    }

    /**
     * Add item to store
     *
     * @param itemStack Item to add
     */
    public void addStoreItem(ItemStack itemStack, String reason) {
        // Call PlayerStoreItemAddEvent
        PlayerStoreItemAddEvent event = new PlayerStoreItemAddEvent(this.getOwnerUniqueId(), itemStack, reason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.getItemStack() != null) {
            storeItems.put(UUID.randomUUID().toString(), event.getItemStack().clone());
        }
    }

    /**
     * Add sale item to store
     *
     * @param saleItem Sale cache item
     */
    public void addStoreItem(SaleCache saleItem, String reason) {
        // Call PlayerStoreItemAddEvent
        PlayerStoreItemAddEvent event = new PlayerStoreItemAddEvent(this.getOwnerUniqueId(), saleItem.getSaleItem(), reason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.getItemStack() != null) {
            storeItems.put(UUID.randomUUID().toString(), event.getItemStack().clone());
        }
    }

    public FileConfiguration saveToConfiguration() {
        FileConfiguration data = new YamlConfiguration();
        for (Map.Entry<String, ItemStack> entry : this.getStoreItems().entrySet()) {
            data.set("items." + entry.getKey(), entry.getValue());
        }
        return data;
    }
}
