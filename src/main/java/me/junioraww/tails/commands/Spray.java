package me.junioraww.tails.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class Spray implements CommandExecutor  {
    /*
    TODO Перенести функционал с плагина ecosocket
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender instanceof Player player) {
            try {
                throw new IOException();
            } catch (IOException e) {
                player.sendRichMessage("<red>Ошибка!");
                e.printStackTrace();
            }

            return true;
        }

        return false;
    }
}
