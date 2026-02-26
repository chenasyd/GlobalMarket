package com.upgrade.globalmarket.internal.command;

import com.upgrade.globalmarket.api.GlobalMarketApi;
import com.upgrade.globalmarket.dto.AbstractStorageHandler;
import com.upgrade.globalmarket.dto.IStorageHandler;
import com.upgrade.globalmarket.internal.handler.PatchHandler;
import com.upgrade.globalmarket.internal.plugin.GlobalMarket;
import com.upgrade.globalmarket.internal.data.DataContainer;
import com.upgrade.globalmarket.api.handler.filter.FilterHandler;
import com.upgrade.globalmarket.api.handler.filter.impl.KeyFilterImpl;
import com.upgrade.globalmarket.api.handler.filter.impl.TypeFilterImpl;
import com.upgrade.globalmarket.internal.i18n.I18n;
import com.upgrade.globalmarket.api.entity.MarketData;
import com.upgrade.globalmarket.internal.gui.impl.StoreContainerGui;
import com.upgrade.globalmarket.internal.util.TextUtil;
import com.google.common.collect.Lists;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Blank038
 */
public class MainCommand implements CommandExecutor, TabCompleter {
    private final GlobalMarket instance;

    public MainCommand(GlobalMarket serverMarket) {
        this.instance = serverMarket;
    }

    public void register() {
        this.instance.getCommand("globalmarket").setExecutor(this);
        this.instance.getCommand("globalmarket").setTabCompleter(this);
    }

    /**
     * CommandExecute?     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (instance.getConfig().getBoolean("command-help")) {
                this.sendHelp(sender, label);
            } else {
                // OpenMarket
                if (sender instanceof Player) {
                    this.openServerMarket(sender, null);
                }
            }
        } else {
            switch (args[0]) {
                case "open":
                    this.openServerMarket(sender, args.length == 1 ? null : args[1]);
                    break;
                case "search":
                    this.searchItemsAndOpenMarket(sender, args);
                    break;
                case "show":
                    this.show(sender);
                    break;
                case "box":
                    if (sender instanceof Player) {
                        new StoreContainerGui((Player) sender, 1, null, null).open(1);
                    }
                    break;
                case "reload":
                    if (sender.hasPermission("servermarket.admin")) {
                        this.instance.loadConfig(false);
                        sender.sendMessage(I18n.getStrAndHeader("reload"));
                    }
                    break;
                case "patch":
                    this.usePatch(sender, args);
                    break;
                case "import":
                    this.importData(sender, args);
                    break;
                default:
                    this.sendHelp(sender, label);
                    break;
            }
        }
        return true;
    }

    private void openServerMarket(CommandSender sender, String key) {
        if (!(sender instanceof Player)) {
            return;
        }
        GlobalMarketApi.openMarket((Player) sender, key, 1, null);
    }

    private void searchItemsAndOpenMarket(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return;
        }
        if (args.length == 1) {
            sender.sendMessage(I18n.getStrAndHeader("wrong-market"));
            return;
        }
        if (args.length == 2) {
            sender.sendMessage(I18n.getStrAndHeader("wrong-key"));
            return;
        }
        FilterHandler builder = new FilterHandler()
                .addKeyFilter(new KeyFilterImpl(args[2]))
                .setTypeFilter(new TypeFilterImpl(Lists.newArrayList("none")));
        GlobalMarketApi.openMarket((Player) sender, args[1], 1, builder);
    }

    private void show(CommandSender sender) {
        for (String line : I18n.getArrayOption("show")) {
            String last = line;
            for (Map.Entry<String, MarketData> entry : DataContainer.MARKET_DATA.entrySet()) {
                String value = "%" + entry.getValue().getMarketKey() + "%";
                if (!last.contains(value)) {
                    continue;
                }
                // Get permission check
                String permission = entry.getValue().getPermission();
                if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
                    last = last.replace(value, I18n.getOption("status-text.no-permission"));
                    continue;
                }
                last = last.replace(value, I18n.getOption("status-text." + entry.getValue().getMarketStatus().name().toLowerCase()));
            }
            sender.sendMessage(TextUtil.formatHexColor(last));
        }
    }

    private void sendHelp(CommandSender sender, String label) {
        for (String text : I18n.getArrayOption("help." + (sender.hasPermission("servermarket.admin") ? "admin" : "default"))) {
            sender.sendMessage(TextUtil.formatHexColor(text).replace("%c", label));
        }
    }

    /**
     * To fix legacy issues from previous versions.
     */
    private void usePatch(CommandSender sender, String[] args) {
        if (!sender.hasPermission("servermarket.patch")) {
            sender.sendMessage(I18n.getStrAndHeader("no-permission"));
            return;
        }
        if (args.length == 1) {
            sender.sendMessage(I18n.getStrAndHeader("wrong-patch-id"));
            return;
        }
        boolean result = PatchHandler.executePatch(args[1]);
        sender.sendMessage(I18n.getStrAndHeader("patch-result." + result));
    }

    private void importData(CommandSender sender, String[] args) {
        if (!sender.hasPermission("servermarket.admin")) {
            return;
        }
        if (args.length == 1) {
            sender.sendMessage(I18n.getStrAndHeader("wrong-source-key"));
            return;
        }
        if (args.length == 2) {
            sender.sendMessage(I18n.getStrAndHeader("wrong-target-key"));
            return;
        }
        IStorageHandler source = AbstractStorageHandler.createStorageHandler(args[1]),
                target = AbstractStorageHandler.createStorageHandler(args[2]);
        if (source == null || target == null) {
            sender.sendMessage(I18n.getStrAndHeader("storage-handler-not-found"));
            return;
        }
        target.initialize();
        target.importData(source.getAllSale());
        sender.sendMessage(I18n.getStrAndHeader("import-data"));
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 2 && strings[0].equalsIgnoreCase("patch")) {
            return new ArrayList<>(PatchHandler.getPatchIds());
        }
        return null;
    }
}
