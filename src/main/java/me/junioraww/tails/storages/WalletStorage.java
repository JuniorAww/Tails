package me.junioraww.tails.storages;

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent;
import com.google.gson.Gson;
import me.junioraww.tails.Main;
import me.junioraww.tails.data.types.wallet.SyncWalletsArray;
import me.junioraww.tails.network.Request;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.HashMap;
import java.util.Map;

public class WalletStorage implements Listener {
    private Map<String, Wallet> wallets = new HashMap<>();
    private Gson gson = new Gson();

    public void add(Wallet wallet) {
        wallets.put(wallet.getName().toLowerCase(), wallet);
    }

    public Wallet getWallet(Player player) {
        String username = player.getName().toLowerCase();
        return wallets.get(username);
    }

    public void initPlayer(String name, String address) {
        Request request = new Request(Request.Action.SYNC, name + ',' + address);

        Main.getPlugin().getClient().sendRequest(request).thenAccept(response -> {
            Bukkit.getLogger().info(response.getArg());

            var array = gson.fromJson(response.getArg(), SyncWalletsArray.class);

            wallets.put(name.toLowerCase(), array.wallets[0]);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void playerJoined(AsyncPlayerPreLoginEvent event) {
        String name = event.getName();
        String address = event.getAddress().getHostAddress();
        initPlayer(name, address);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerLeft(PlayerConnectionCloseEvent event) {
        String name = event.getPlayerName().toLowerCase();

        // TODO may be asynchronous

        if (wallets.containsKey(name)) wallets.remove(name);
    }
}
