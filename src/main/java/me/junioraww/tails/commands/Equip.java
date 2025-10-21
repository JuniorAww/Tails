package me.junioraww.tails.commands;

import me.junioraww.tails.Main;
import me.junioraww.tails.data.types.items.Attach;
import me.junioraww.tails.data.types.wallet.Item;
import me.junioraww.tails.storages.Wallet;
import me.junioraww.tails.utils.SpawnAttach;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class Equip implements CommandExecutor {
    /*
    TODO одеть, снять аксессуар/питомца
     */

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender instanceof Player player) {
            try {
                Wallet wallet = Main.getPlugin().getStorage().getWallet(player);
                if (wallet == null) throw new RuntimeException("Кошелек не найден");

                Optional<Item> optional = wallet.getItems().stream()/*.filter(item -> item.isEquipped())*/.findFirst();

                if (optional.isPresent()) {
                    if (optional.get() instanceof Attach attach) {
                        if (!SpawnAttach.isEquipped(player)) {
                            SpawnAttach.wear(player, attach);
                        } else {
                            SpawnAttach.unwear(player);
                        }
                    }
                }
            } catch (Exception e) {
                player.sendRichMessage("<red>Ошибка!");
                e.printStackTrace();
            }

            return true;
        }

        return false;
    }
}
