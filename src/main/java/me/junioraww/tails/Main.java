package me.junioraww.tails;

import me.junioraww.tails.commands.Equip;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private static Main plugin;
    public static Main getPlugin() { return plugin; }

    @Override
    public void onEnable() {
        plugin = this;

        Equip equip = new Equip();
        this.getCommand("equip").setExecutor(equip);

    }

    @Override
    public void onDisable() {
        plugin = null;

    }
}
