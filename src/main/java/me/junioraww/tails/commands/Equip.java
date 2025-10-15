package me.junioraww.tails.commands;

import me.junioraww.tails.utils.TailSpawn;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class Equip implements CommandExecutor {
    /*
    TODO одеть, снять аксессуар/питомца
     */

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender instanceof Player player) {
            try {
                if (!TailSpawn.isEquipped(player)) {
                    TailSpawn.wear(player, "plugins/Tails/fox.json");
                } else {
                    TailSpawn.unwear(player);
                }
            } catch (IOException e) {
                player.sendRichMessage("<red>Ошибка!");
                e.printStackTrace();
            }

            return true;
        }

        return false;
    }
}
