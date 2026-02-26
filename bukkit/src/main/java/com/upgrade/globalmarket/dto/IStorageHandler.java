package com.upgrade.globalmarket.dto;

import com.upgrade.globalmarket.internal.cache.other.OfflineTransactionData;
import com.upgrade.globalmarket.internal.cache.other.SaleLog;
import com.upgrade.globalmarket.internal.cache.player.PlayerCache;
import com.upgrade.globalmarket.internal.cache.sale.SaleCache;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Blank038
 */
public interface IStorageHandler {

    /**
     * Called when IStorageHandler is initialized.
     */
    void initialize();

    /**
     * Called when IStorageHandler is load/reload.
     */
    void reload();

    /**
     * Storage Controller data
     *
     * @param market Market
     */
    void load(String market);

    /**
     * Determine if item exists at market
     *
     * @param market Market
     * @param saleId Item
     * @return true if exists, false otherwise
     */
    boolean hasSale(String market, String saleId);

    /**
     * Get count of items at market for player
     *
     * @param uuid   target player UUID
     * @param market Market
     * @return item count
     */
    int getSaleCountByPlayer(UUID uuid, String market);

    /**
     * Get item data
     *
     * @param market Market
     * @param saleId Item
     * @return ItemData
     */
    Optional<SaleCache> getSaleItem(String market, String saleId);

    /**
     * Get all items from market
     *
     * @param market Market
     * @return MarketItemSet
     */
    Map<String, SaleCache> getSaleItemsByMarket(String market);

    List<SaleCache> getAllSale();

    /**
     * Remove item from market and return it, if not exists return null
     *
     * @param market Market
     * @param saleId Item
     * @return ItemData
     */
    Optional<SaleCache> removeSaleItem(String market, String saleId);

    /**
     * Add item to market
     *
     * @param market   Market
     * @param saleItem ItemData
     */
    boolean addSale(String market, SaleCache saleItem);

    /**
     * Add a sale log
     *
     * @param log sale log
     */
    void addLog(SaleLog log);

    /**
     * Save item data, for YAML mode save to disk
     *
     * @param market Market
     * @param map    ItemData
     */
    void save(String market, Map<String, SaleCache> map);

    /**
     * Remove timeout items
     */
    void removeTimeOutItem();

    /**
     * Save all data, for YAML mode save to disk
     */
    void saveAll();

    void saveAllPlayerData();

    void importData(List<SaleCache> saleCaches);

    /**
     * Save player data
     *
     * @param uuid target player
     */
    void savePlayerData(UUID uuid, boolean removeCache);

    /**
     * Save player data
     *
     * @param playerCache Data
     */
    void savePlayerData(PlayerCache playerCache, boolean removeCache);

    void setLock(UUID uuid, boolean locked);

    boolean isLocked(UUID uuid);

    /**
     * Get cache data or load from disk if data exists
     *
     * @param uuid target player
     * @return Data
     */
    PlayerCache getOrLoadPlayerCache(UUID uuid, boolean forceLoad);

    /**
     * Get data from cache if data exists, otherwise return Optional.empty()
     *
     * @param uuid target player
     * @return Data
     */
    Optional<PlayerCache> getPlayerDataByCache(UUID uuid);

    /**
     * Add item to player store
     *
     * @param uuid      target player
     * @param itemStack Storage
     * @return true if successful, false otherwise
     */
    boolean addItemToStore(UUID uuid, ItemStack itemStack, String reason);

    /**
     * Add item to player store
     *
     * @param uuid     target player
     * @param saleItem StorageItem
     * @return true if successful, false otherwise
     */
    boolean addItemToStore(UUID uuid, SaleCache saleItem, String reason);

    /**
     * Remove item from player store
     *
     * @param uuid        target player
     * @param storeItemId
     * @return RemoveResult
     */
    ItemStack removeStoreItem(UUID uuid, String storeItemId);

    /**
     * Add an offline transaction record
     *
     * @param data offlineTransactionData
     */
    void addOfflineTransaction(OfflineTransactionData data);

    /**
     * Remove an offline transaction record
     *
     * @param key offlineTransactionData key
     * @return remove result
     */
    boolean removeOfflineTransaction(String key);

    /**
     * Get the player's offline transaction record Map
     *
     * @param ownerUniqueId target player uuid
     * @return offlineTransactionData Map
     */
    Map<String, OfflineTransactionData> getOfflineTransactionByPlayer(UUID ownerUniqueId);
}
