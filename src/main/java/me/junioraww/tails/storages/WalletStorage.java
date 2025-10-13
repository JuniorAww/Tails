package me.junioraww.tails.storages;

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent;
import com.destroystokyo.paper.event.player.PlayerHandshakeEvent;
import com.google.gson.Gson;
import me.junioraww.tails.Main;
import me.junioraww.tails.network.Request;
import me.junioraww.tails.network.Response;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class WalletStorage implements Listener {
    private Map<String, Wallet> wallets = new HashMap<>();
    private Gson gson = new Gson();

    public Wallet getWallet(Player player) {
        String username = player.getName().toLowerCase();
        return wallets.get(username);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerJoined(AsyncPlayerPreLoginEvent event) {
        String name = event.getName();
        String address = event.getAddress().getHostAddress();

        Request request = new Request(Request.Action.SYNC, name + ',' + address);

        try {
            Response response = Main.getPlugin().getClient().sendRequest(request);
            Bukkit.getLogger().info(response.getArg());

            Wallet wallet = gson.fromJson(response.getArg(), Wallet.class);

            wallets.put(name.toLowerCase(), wallet);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerJoined(PlayerConnectionCloseEvent event) {
        String name = event.getPlayerName().toLowerCase();

        // TODO may be asynchronous

        if (wallets.containsKey(name)) wallets.remove(name);
    }
}
