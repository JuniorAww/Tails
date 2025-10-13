package me.junioraww.tails;

import me.junioraww.tails.commands.Equip;
import me.junioraww.tails.commands.Money;
import me.junioraww.tails.commands.Spray;
import me.junioraww.tails.storages.PlayerCache;
import me.junioraww.tails.storages.WalletStorage;
import me.junioraww.tails.utils.TLSClient;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class Main extends JavaPlugin {
    private static Main plugin;
    public static Main getPlugin() { return plugin; }

    private TLSClient client;
    private WalletStorage storage;
    private PlayerCache players;

    public TLSClient getClient() {
        return client;
    }

    public WalletStorage getStorage() {
        return storage;
    }
    /*
    TODO Локализация на 3 языка
     */

    @Override
    public void onEnable() {
        plugin = this;
        client = new TLSClient();
        storage = new WalletStorage();
        players = new PlayerCache();

        try {
            getConfig().save("config.yml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            client.init();
            load();
        } catch (RuntimeException e) {
            e.printStackTrace();
            getLogger().warning("Плагин будет отключён");
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void load() {
        Equip equip = new Equip();
        Spray spray = new Spray();
        this.getCommand("equip").setExecutor(equip);
        this.getCommand("spray").setExecutor(spray);
        this.getCommand("money").setExecutor(new Money());
        //this.getCommand("items").setExecutor(items)

        getServer().getPluginManager().registerEvents(storage, this);
    }

    @Override
    public void onDisable() {
        plugin = null;
        client.stop();
    }
}
