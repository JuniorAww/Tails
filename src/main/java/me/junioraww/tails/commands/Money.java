package me.junioraww.tails.commands;

import me.junioraww.tails.Main;
import me.junioraww.tails.network.Request;
import me.junioraww.tails.storages.Wallet;
import me.junioraww.tails.utils.Locales;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Money implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender instanceof Player player) {
            if (args.length == 2 && args[0].equals("cheat") && player.isOp()) {
                var request = new Request(Request.Action.ADD_TO_BALANCE, player.getName() + "," + Long.parseLong(args[1]));
                Main.getPlugin().getClient().sendRequest(request).thenAccept(response -> {
                    Wallet wallet = Main.getPlugin().getStorage().getWallet(player);
                    wallet.setBalance(Long.parseLong(response.getArg()));
                    player.sendRichMessage("<green>Ваш игровой счет: <yellow>" + wallet.getBalance());
                });
            } else {
                Wallet wallet = Main.getPlugin().getStorage().getWallet(player);
                Locales.Text.Balance.send(player, wallet.getBalance());
                // <green>Ваш игровой счет: <yellow>%0
            }
            return true;
        }

        return false;
    }
}
