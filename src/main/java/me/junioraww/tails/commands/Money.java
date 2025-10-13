package me.junioraww.tails.commands;

import me.junioraww.tails.Main;
import me.junioraww.tails.storages.Wallet;
import me.junioraww.tails.utils.TailSpawn;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class Money implements CommandExecutor {
    /*
    TODO одеть, снять аксессуар/питомца
     */

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender instanceof Player player) {
            Wallet wallet = Main.getPlugin().getStorage().getWallet(player);
            player.sendRichMessage("<green>Ваш игровой счет: <yellow>" + wallet.getBalance());

            return true;
        }

        return false;
    }
}
