package me.junioraww.tails.commands;

import me.junioraww.tails.Main;
import me.junioraww.tails.network.Request;
import me.junioraww.tails.storages.Wallet;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Items implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender instanceof Player player) {
            Wallet wallet = Main.getPlugin().getStorage().getWallet(player);

            return true;
        }

        return false;
    }
}

