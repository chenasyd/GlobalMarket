package com.upgrade.globalmarket.internal.service.notify.impl.mysql;

import com.aystudio.core.bukkit.util.mysql.MySqlStorageHandler;
import com.upgrade.globalmarket.api.GlobalMarketApi;
import com.upgrade.globalmarket.internal.cache.other.NotifyCache;
import com.upgrade.globalmarket.internal.plugin.GlobalMarket;
import com.upgrade.globalmarket.internal.service.notify.impl.IncreaseNotifyService;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;

/**
 * @author Blank038
 */
public class MySQLNotifyServiceImpl extends IncreaseNotifyService {
    private MySqlStorageHandler storageHandler;
    private int updateWindow = 120;

    @Override
    public void register(ConfigurationSection config) {
        int fetchInterval = config.getInt("fetch-interval");
        this.updateWindow = config.getInt("update-window");
        this.runTask(fetchInterval);

        String[] sqlArray = {
                "CREATE TABLE IF NOT EXISTS servermarket_notifies(`id` INT AUTO_INCREMENT, `message` TEXT NOT NULL, `push_date` TIMESTAMP, PRIMARY KEY (`id`)) ENGINE = InnoDB DEFAULT CHARSET = UTF8;"
        };
        this.storageHandler = new MySqlStorageHandler(
                GlobalMarket.getInstance(),
                config.getString("url"),
                config.getString("user"),
                config.getString("password"),
                sqlArray
        );
        this.storageHandler.setReconnectionQueryTable("servermarket_notifies");
    }

    @Override
    public void update() {
        this.storageHandler.connect((statement) -> {
            try {
                Timestamp timestamp = new Timestamp(System.currentTimeMillis() - (this.updateWindow * 1000L));
                statement.setTimestamp(1, timestamp);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    NotifyCache notifyCache = new NotifyCache();
                    notifyCache.index = resultSet.getInt(1);
                    notifyCache.message = resultSet.getString(2);
                    notifyCache.time = resultSet.getTimestamp(3).getTime();
                    if (this.hasIndex(notifyCache.index)) {
                        continue;
                    }
                    this.addIndex(notifyCache.index);
                    this.broadcast(notifyCache.message);
                }
                resultSet.close();
            } catch (SQLException e) {
                GlobalMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "Failed to update notifies.");
            }
        }, "SELECT * FROM servermarket_notifies WHERE push_date >= ?;");
    }

    @Override
    public void push(NotifyCache notifyCache) {
        GlobalMarketApi.getPlatformApi().runTaskAsynchronously(GlobalMarket.getInstance(), () -> {
            this.storageHandler.connect((statement) -> {
                try {
                    Timestamp timestamp = new Timestamp(notifyCache.time);
                    statement.setString(1, notifyCache.message);
                    statement.setTimestamp(2, timestamp);
                    statement.executeUpdate();
                } catch (SQLException e) {
                    GlobalMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "Failed to push notify cache.");
                }
            }, "INSERT INTO servermarket_notifies (message, push_date) VALUES (?, ?);");
        });
    }
}
