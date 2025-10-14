package me.junioraww.tails;

import com.google.gson.Gson;
import me.junioraww.tails.commands.Equip;
import me.junioraww.tails.commands.Money;
import me.junioraww.tails.commands.Spray;
import me.junioraww.tails.data.types.wallet.SyncWalletsArray;
import me.junioraww.tails.network.Request;
import me.junioraww.tails.network.Response;
import me.junioraww.tails.storages.PlayerCache;
import me.junioraww.tails.storages.WalletStorage;
import me.junioraww.tails.utils.TLSClient;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

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

        if (!getDataFolder().exists()) getDataFolder().mkdirs();

        try {
            getConfig().save("config.yml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
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

        Bukkit.getAsyncScheduler().runNow(this, task -> {
            client.init();
            var online = Bukkit.getOnlinePlayers();
            if (!online.isEmpty()) {
                String usernames = online.stream()
                        .map(player -> player.getName() + "," + player.getAddress().getHostName())
                        .collect(Collectors.joining(";"));

                client.sendRequest(new Request(Request.Action.SYNC, usernames)).thenAccept(response -> {
                    Bukkit.getLogger().info(response.toString());
                    SyncWalletsArray array = new Gson().fromJson(response.getArg(), SyncWalletsArray.class);
                    for (var wallet : array.wallets) {
                        storage.add(wallet);
                    }
                });
            }
        });
    }

    @Override
    public void onDisable() {
        plugin = null;
        client.stop();
    }
}
